package jp.techacademy.ushio.zaizen.taskapp2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*

const val EXTRA_TASK = "jp.techacademy.ushio.zaizen.taskapp2.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        //Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        //ListViewの設定
        mTaskAdapter = TaskAdapter(this)


        select_button.setOnClickListener {
            hideKeyboard()
            reloadListView()
            }


        //ListViewをタップした時の処理
        listView1.setOnItemClickListener { parent, view, position, id ->
            //入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }


        //ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            //タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            //ダイアログを表示
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()
            true

        }

        reloadListView()
    }


private fun hideKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val manager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}


    private fun reloadListView() {
        //Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        var scategory = select_category_text.text.toString()
        
        var taskRealmResults =
            if (scategory.isNotEmpty()) {
                mRealm.where(Task::class.java).equalTo("category", scategory).findAll()
                    .sort("date", Sort.DESCENDING)
            } else {
                mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
            }
        /*
            Log.d("uztest","scategory="+scategory.toString()+"!!!")
            if (scategory == "") {
                Log.d("uztest", "null")
            } else {
                Log.d("uztest", "not null")
            }
            */

        //上記の結果をTaskListとしてセットする
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

        //TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        //表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }
}


    /*
    private fun addTaskForTest(){
        val task = Task()
        task.title="作業"
        task.contents="プログラムを書いてPushする"
        task.date = Date()
        task.id = 0
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()
    }
    */

