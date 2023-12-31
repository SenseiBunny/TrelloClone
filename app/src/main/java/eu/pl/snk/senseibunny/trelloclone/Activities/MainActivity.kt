package eu.pl.snk.senseibunny.trelloclone.Activities

import Firebase.FireStoreClass
import adapters.BoardAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import eu.pl.snk.senseibunny.trelloclone.R
import eu.pl.snk.senseibunny.trelloclone.databinding.ActivityMainBinding
import models.Board
import models.User

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    var binding : ActivityMainBinding ?=null

    private lateinit var userName: String

    private val startUpdateActivityAndGetResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                FireStoreClass().signInUser(this) //what should be done if next activity end
            } else {
                Log.e("onActivityResult()", "Profile update cancelled by user")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        FireStoreClass().signInUser(this, true)

        val boardButton= findViewById<FloatingActionButton>(R.id.floatinButton)
        boardButton.setOnClickListener{
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("name",userName)
            startActivity(intent)
        }



    }

    fun poupulateBoardsList(boardList: ArrayList<Board>){
        hideProgressDialog()

        if(boardList.size>0){
            binding?.appbar?.main?.boardList?.visibility=View.VISIBLE
            binding?.appbar?.main?.text?.visibility=View.GONE

            val adapter = BoardAdapter(this, boardList)
            binding?.appbar?.main?.boardList?.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            binding?.appbar?.main?.boardList?.adapter = adapter
        }
        else{
            binding?.appbar?.main?.boardList?.visibility=View.GONE
            binding?.appbar?.main?.text?.visibility=View.VISIBLE
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.appbar?.toolbarMainActivity)
        binding?.appbar?.toolbarMainActivity?.setNavigationIcon(R.drawable.baseline_density_medium_24)

        binding?.appbar?.toolbarMainActivity?.setNavigationOnClickListener(){
            //toogle drawer
            toogleDrawer()
        }

        binding?.navView!!.setNavigationItemSelectedListener(this)
    }

    private fun toogleDrawer(){
        if(binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START) //close drawer
        }else{
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() { //implements closing the drawer with back button
        if(binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START) //close drawer
        }else{
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startUpdateActivityAndGetResult.launch(Intent(this, ProfileActivity::class.java))

            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, StartActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) //idk what it does xd
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout!!.closeDrawer(GravityCompat.START)

        return true
    }

    fun UpdateNavigationUserDetails(user: User, readBoardList: Boolean){
        val imageView = findViewById<CircleImageView>(R.id.user_image)
        userName = user.name.toString()
        user.image?.let { loadImageFromUrl(this, it,imageView) }
        val textView = findViewById<TextView>(R.id.tv_username)
        if(user.name!=null){
            textView.setText(user.name.toString())
        }

        if(readBoardList){
            showProgressDialog()
            FireStoreClass().getBoardsList(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }

}