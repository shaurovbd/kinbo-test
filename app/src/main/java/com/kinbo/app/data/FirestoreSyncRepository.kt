package com.kinbo.app.data

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kinbo.app.model.Budget
import com.kinbo.app.model.CategoryShare
import com.kinbo.app.model.Collaborator
import com.kinbo.app.model.ExpenseEntry
import com.kinbo.app.model.ItemCategory
import com.kinbo.app.model.KinboNotification
import com.kinbo.app.model.ListRole
import com.kinbo.app.model.Priority
import com.kinbo.app.model.ShoppingItem
import com.kinbo.app.model.ShoppingList
import com.kinbo.app.model.User
import com.kinbo.app.model.WeeklySpending
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Real-time collaboration backend backed by Firebase Firestore.
 *
 * ACTIVATION:
 *  1. Create a Firebase project at https://console.firebase.google.com
 *  2. Add an Android app (package: com.kinbo.app) and download google-services.json
 *  3. Place google-services.json in /app/ and apply the google-services Gradle plugin
 *     (see README → "Activating real-time collaboration")
 *  4. Enable Firestore + Email/Password Auth in the Firebase console
 *  5. [SyncManager] will then auto-select this repository instead of the local one
 *
 * Until then, this class compiles and ships but is not instantiated at runtime.
 *
 * DATA MODEL (Firestore collections):
 *   users/{uid}            → name, email, initials, plan, premium
 *   lists/{listId}         → name, emoji, ownerId, shareCode, archived, favorite, createdAt, updatedAt, lastModifiedBy
 *   lists/{listId}/items/{itemId} → name, qty, unit, price, category, priority, purchased, note
 *   lists/{listId}/members/{uid}  → name, role, initials, colorIndex, email
 *
 * Real-time: a snapshot listener on the user's lists keeps [lists] live across devices.
 */
class FirestoreSyncRepository : ListRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    private val _user = MutableStateFlow(User("Guest", "", "G"))
    override val user: StateFlow<User> = _user.asStateFlow()

    private val _lists = MutableStateFlow<List<ShoppingList>>(emptyList())
    override val lists: StateFlow<List<ShoppingList>> = _lists.asStateFlow()

    private val _budget = MutableStateFlow(Budget(monthlyLimit = 500.0, spent = 0.0))
    override val budget: StateFlow<Budget> = _budget.asStateFlow()

    private val _expenses = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    override val expenses: StateFlow<List<ExpenseEntry>> = _expenses.asStateFlow()

    private val _notifications = MutableStateFlow<List<KinboNotification>>(emptyList())
    override val notifications: StateFlow<List<KinboNotification>> = _notifications.asStateFlow()

    private val _weeklySpending = MutableStateFlow<List<WeeklySpending>>(emptyList())
    override val weeklySpending: StateFlow<List<WeeklySpending>> = _weeklySpending.asStateFlow()

    private val _favorites = MutableStateFlow<List<String>>(emptyList())
    override val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    private val _premium = MutableStateFlow(false)
    override val premium: StateFlow<Boolean> = _premium.asStateFlow()

    override val isSynced: Boolean = true

    private var listsListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { fa ->
            val u = fa.currentUser
            if (u != null) attachListeners(u.uid) else { detachListeners(); _user.value = User("Guest", "", "G") }
        }
    }

    private fun attachListeners(uid: String) {
        detachListeners()
        // User profile
        userListener = db.collection("users").document(uid).addSnapshotListener { snap, e ->
            if (e != null) { Log.w(TAG, "user listen", e); return@addSnapshotListener }
            val d = snap?.data ?: return@addSnapshotListener
            _user.value = User(
                name = d["name"] as? String ?: "User",
                email = d["email"] as? String ?: "",
                initials = d["initials"] as? String ?: "U",
                plan = d["plan"] as? String ?: "Free",
                premium = d["premium"] as? Boolean ?: false,
            )
        }
        // Lists where I'm a member — real-time across devices
        listsListener = db.collection("lists").whereArrayContains("memberUids", uid)
            .addSnapshotListener { snap, e ->
                if (e != null) { Log.w(TAG, "lists listen", e); return@addSnapshotListener }
                val fetched = snap?.documents.orEmpty().mapNotNull { doc -> docToLightList(doc.id, doc.data) }
                // Items are subcollections; fetch per list
                _lists.value = fetched
                fetched.forEach { loadItems(it.id) }
            }
    }

    private fun detachListeners() {
        listsListener?.remove(); listsListener = null
        userListener?.remove(); userListener = null
    }

    private fun docToLightList(id: String, data: Map<String, Any>?): ShoppingList? {
        data ?: return null
        @Suppress("UNCHECKED_CAST")
        val members = (data["members"] as? List<Map<String, Any>>)?.map { m ->
            Collaborator(
                name = m["name"] as? String ?: "",
                role = ListRole.valueOf(m["role"] as? String ?: "Viewer"),
                initials = m["initials"] as? String ?: "",
                colorIndex = (m["colorIndex"] as? Long)?.toInt() ?: 0,
                userId = m["userId"] as? String ?: "",
                email = m["email"] as? String ?: "",
            )
        } ?: emptyList()
        return ShoppingList(
            id = id,
            name = data["name"] as? String ?: "Untitled",
            emoji = data["emoji"] as? String ?: "🛒",
            favorite = data["favorite"] as? Boolean ?: false,
            archived = data["archived"] as? Boolean ?: false,
            collaborators = members,
            createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis(),
            ownerId = data["ownerId"] as? String ?: "",
            shareCode = data["shareCode"] as? String ?: "",
            lastModifiedBy = data["lastModifiedBy"] as? String ?: "",
            items = emptyList(), // loaded asynchronously below
        )
    }

    private fun loadItems(listId: String) {
        db.collection("lists").document(listId).collection("items")
            .addSnapshotListener { snap, e ->
                if (e != null) { Log.w(TAG, "items listen $listId", e); return@addSnapshotListener }
                val items = snap?.documents.orEmpty().mapNotNull { doc ->
                    val d = doc.data ?: return@mapNotNull null
                    ShoppingItem(
                        id = doc.id,
                        name = d["name"] as? String ?: "",
                        quantity = (d["quantity"] as? Number)?.toDouble() ?: 1.0,
                        unit = d["unit"] as? String ?: "pcs",
                        note = d["note"] as? String ?: "",
                        price = (d["price"] as? Number)?.toDouble() ?: 0.0,
                        category = runCatching { ItemCategory.valueOf(d["category"] as? String ?: "") }.getOrDefault(ItemCategory.Other),
                        priority = runCatching { Priority.valueOf(d["priority"] as? String ?: "") }.getOrDefault(Priority.Medium),
                        purchased = d["purchased"] as? Boolean ?: false,
                    )
                }
                _lists.update { lists -> lists.map { if (it.id == listId) it.copy(items = items) else it } }
            }
    }

    private val currentUid: String get() = auth.currentUser?.uid ?: ""

    // ---- Category share derived from expenses ----
    override fun categoryShare(): List<CategoryShare> {
        val grouped = _expenses.value.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
        return grouped.map { CategoryShare(it.key, it.value) }.sortedByDescending { it.amount }
    }

    // ---- List operations → Firestore writes ----
    override fun toggleFavorite(id: String) {
        val list = _lists.value.firstOrNull { it.id == id } ?: return
        db.collection("lists").document(id).update("favorite", !list.favorite, "updatedAt", System.currentTimeMillis())
    }

    override fun archiveList(id: String) {
        db.collection("lists").document(id).update("archived", true, "updatedAt", System.currentTimeMillis())
    }

    override fun deleteList(id: String) {
        db.collection("lists").document(id).delete()
    }

    override fun duplicateList(id: String) {
        val src = _lists.value.firstOrNull { it.id == id } ?: return
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val data = mapOf(
            "name" to "${src.name} (Copy)", "emoji" to src.emoji, "ownerId" to currentUid,
            "shareCode" to "", "archived" to false, "favorite" to false,
            "createdAt" to now, "updatedAt" to now, "lastModifiedBy" to currentUid,
            "memberUids" to listOf(currentUid),
        )
        db.collection("lists").document(newId).set(data)
        src.items.forEach { item ->
            db.collection("lists").document(newId).collection("items").document(item.id)
                .set(item.toMap())
        }
    }

    override fun renameList(id: String, newName: String) {
        db.collection("lists").document(id).update("name", newName, "updatedAt", System.currentTimeMillis(), "lastModifiedBy", currentUid)
    }

    override fun addList(name: String, emoji: String): ShoppingList {
        val uid = currentUid
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val me = Collaborator(_user.value.name, ListRole.Owner, _user.value.initials, 0, uid, _user.value.email)
        val data = mapOf(
            "name" to name, "emoji" to emoji, "ownerId" to uid, "shareCode" to "",
            "archived" to false, "favorite" to false, "createdAt" to now, "updatedAt" to now,
            "lastModifiedBy" to uid, "memberUids" to listOf(uid),
            "members" to listOf(memberMap(me)),
        )
        db.collection("lists").document(id).set(data)
        // optimistic local update
        val list = ShoppingList(id = id, name = name, emoji = emoji, ownerId = uid, collaborators = listOf(me))
        _lists.update { listOf(list) + it }
        return list
    }

    override fun getList(id: String): ShoppingList? = _lists.value.firstOrNull { it.id == id }

    // ---- Item operations → Firestore writes ----
    override fun addItem(listId: String, item: ShoppingItem) {
        db.collection("lists").document(listId).collection("items").document(item.id).set(item.toMap())
        db.collection("lists").document(listId).update("updatedAt", System.currentTimeMillis(), "lastModifiedBy", currentUid)
    }

    override fun updateItem(listId: String, item: ShoppingItem) {
        db.collection("lists").document(listId).collection("items").document(item.id).set(item.toMap())
        db.collection("lists").document(listId).update("updatedAt", System.currentTimeMillis(), "lastModifiedBy", currentUid)
    }

    override fun removeItem(listId: String, itemId: String) {
        db.collection("lists").document(listId).collection("items").document(itemId).delete()
        db.collection("lists").document(listId).update("updatedAt", System.currentTimeMillis(), "lastModifiedBy", currentUid)
    }

    override fun togglePurchased(listId: String, itemId: String) {
        val list = _lists.value.firstOrNull { it.id == listId } ?: return
        val item = list.items.firstOrNull { it.id == itemId } ?: return
        db.collection("lists").document(listId).collection("items").document(itemId)
            .update("purchased", !item.purchased)
        db.collection("lists").document(listId).update("updatedAt", System.currentTimeMillis(), "lastModifiedBy", currentUid)
    }

    override fun sortItemsByCategory(listId: String) {
        val list = _lists.value.firstOrNull { it.id == listId } ?: return
        val sorted = ShoppingAssistant.sortByCategory(list.items)
        // re-write order by updating a sortIndex field
        sorted.forEachIndexed { index, item ->
            db.collection("lists").document(listId).collection("items").document(item.id)
                .update("sortIndex", index)
        }
    }

    // ---- Budget / notifications (local for now; can be moved to Firestore later) ----
    override fun setBudget(limit: Double) { _budget.value = _budget.value.copy(monthlyLimit = limit) }
    override fun markAllNotificationsRead() = _notifications.update { it.map { n -> n.copy(read = true) } }

    // ---- Auth → Firebase Auth ----
    override fun login(email: String) {
        auth.signInWithEmailAndPassword(email, "kinbo-default")
            .addOnFailureListener { Log.w(TAG, "login failed", it) }
    }

    override fun signup(name: String, email: String) {
        auth.createUserWithEmailAndPassword(email, "kinbo-default")
            .addOnSuccessListener { res ->
                val uid = res.user?.uid ?: return@addOnSuccessListener
                db.collection("users").document(uid).set(mapOf(
                    "name" to name, "email" to email, "initials" to name.take(2).uppercase(),
                    "plan" to "Free", "premium" to false,
                ))
            }
            .addOnFailureListener { Log.w(TAG, "signup failed", it) }
    }

    override fun setPremium(v: Boolean) {
        _premium.value = v
        val uid = currentUid
        if (uid.isNotEmpty()) db.collection("users").document(uid)
            .update("premium", v, "plan", if (v) "Premium" else "Free")
    }

    // ---- Collaboration ----
    override fun generateShareCode(listId: String): String {
        val code = (1..6).map { ('A'..'Z') + ('0'..'9') }.joinToString("") { it.random().toString() }
        db.collection("lists").document(listId).update("shareCode", code)
        return code
    }

    override fun joinListByShareCode(shareCode: String): ShoppingList? {
        db.collection("lists").whereEqualTo("shareCode", shareCode).get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull() ?: return@addOnSuccessListener
                val uid = currentUid
                if (uid.isEmpty()) return@addOnSuccessListener
                // add me as a Viewer member
                val me = Collaborator(_user.value.name, ListRole.Viewer, _user.value.initials, 0, uid, _user.value.email)
                db.collection("lists").document(doc.id)
                    .update("memberUids", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
                db.collection("lists").document(doc.id)
                    .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(memberMap(me)))
            }
        return null // async; the listener will surface the joined list
    }

    override fun addCollaborator(listId: String, collaborator: Collaborator) {
        db.collection("lists").document(listId)
            .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(memberMap(collaborator)))
        if (collaborator.userId.isNotEmpty()) {
            db.collection("lists").document(listId)
                .update("memberUids", com.google.firebase.firestore.FieldValue.arrayUnion(collaborator.userId))
        }
    }

    override fun removeCollaborator(listId: String, userId: String) {
        val list = _lists.value.firstOrNull { it.id == listId } ?: return
        val toRemove = list.collaborators.firstOrNull { it.userId == userId } ?: return
        db.collection("lists").document(listId)
            .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(memberMap(toRemove)))
        if (userId.isNotEmpty()) {
            db.collection("lists").document(listId)
                .update("memberUids", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
        }
    }

    override fun updateCollaboratorRole(listId: String, userId: String, role: ListRole) {
        val list = _lists.value.firstOrNull { it.id == listId } ?: return
        val updated = list.collaborators.map { if (it.userId == userId) it.copy(role = role) else it }
        db.collection("lists").document(listId).update("members", updated.map { memberMap(it) })
    }

    private fun memberMap(c: Collaborator): Map<String, Any> = mapOf(
        "name" to c.name, "role" to c.role.name, "initials" to c.initials,
        "colorIndex" to c.colorIndex, "userId" to c.userId, "email" to c.email,
    )

    private fun ShoppingItem.toMap(): Map<String, Any> = mapOf(
        "name" to name, "quantity" to quantity, "unit" to unit, "note" to note,
        "price" to price, "category" to category.name, "priority" to priority.name,
        "purchased" to purchased,
    )

    companion object {
        private const val TAG = "KinboFirestore"

        /**
         * Returns true if Firebase has been initialized with real project config
         * (i.e. google-services.json was present at app start).
         */
        fun isConfigured(): Boolean {
            return try {
                val app = FirebaseApp.getInstance(FirebaseApp.DEFAULT_APP_NAME)
                val opts = app.options
                !opts.apiKey.isNullOrEmpty() && !opts.projectId.isNullOrEmpty() && opts.apiKey != "PLACEHOLDER_API_KEY"
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Manual initialization for environments where the google-services Gradle
         * plugin is not applied. Call once at startup with values from google-services.json.
         */
        fun initialize(context: android.content.Context, apiKey: String, appId: String, projectId: String) {
            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setApplicationId(appId)
                .setProjectId(projectId)
                .build()
            try {
                FirebaseApp.initializeApp(context, options)
            } catch (e: Exception) {
                Log.w(TAG, "Firebase already initialized or init failed", e)
            }
        }
    }
}
