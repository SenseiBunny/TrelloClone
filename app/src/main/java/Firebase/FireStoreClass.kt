package Firebase

import android.app.Activity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import eu.pl.snk.senseibunny.trelloclone.Activities.*
import models.Board
import models.User

class FireStoreClass: BaseActivity() {

    private val mFirestore = FirebaseFirestore.getInstance()


    fun signInUser(activity: Activity, readBoardList:Boolean = false){
        mFirestore.collection("Users")//creating collection
            .document(getCurrentUserId()).get().addOnSuccessListener {document->
                val loggedInUser = document.toObject(User::class.java)//we retrieve from collection and make a user class
                if (loggedInUser != null) {
                    when(activity){
                        is SingInActivity ->{
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity ->{
                            activity.UpdateNavigationUserDetails(loggedInUser, readBoardList)
                        }
                        is ProfileActivity->{
                            activity.profileDetails(loggedInUser)
                        }
                    }

                }
            }.addOnFailureListener{
                hideProgressDialog()
            }
    }
    //we register user also in our database
    public fun registerUser(activity:SingUpActivity, userInfo: User){
        mFirestore.collection("Users")//creating collection
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge()).addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }

    public fun createBoard(activity:BoardActivity, board: Board){
        mFirestore.collection("Boards")//creating collection
            .document().set(board, SetOptions.merge()).addOnSuccessListener {
                Toast.makeText(activity, "board created", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener{
                Toast.makeText(activity, "board error", Toast.LENGTH_SHORT).show()

            }
    }



    fun updateUserProfile(activity: ProfileActivity, userHashMap:HashMap<String, Any>){
        mFirestore.collection("Users")//creating collection
            .document(getCurrentUserId()).update(userHashMap).addOnSuccessListener {
                Toast.makeText(activity,"Update success", Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener{
                activity.hideProgressDialog()
                Toast.makeText(activity,"Update failure", Toast.LENGTH_LONG).show()
            }
    }

    fun getBoardsList(activity: MainActivity){
        mFirestore.collection("Boards")
            .whereArrayContains("assignedTo", getCurrentUserId()) //checking boards collection if board is assigned to current user
            .get().addOnSuccessListener {
                document ->
                    val boardList: ArrayList<Board> = ArrayList()
                    for(i in document){
                        boardList.add(i.toObject(Board::class.java))
                        activity.poupulateBoardsList(boardList)
                    }

            }.addOnFailureListener{
                hideProgressDialog()
                Toast.makeText(activity, "Failure", Toast.LENGTH_LONG).show()
            }
    }

    fun getCurrentUserId():String {
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""

        if(currentUser!=null){
            currentUserId=currentUser.uid
        }

        return currentUserId
    }
}