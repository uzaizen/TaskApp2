package jp.techacademy.ushio.zaizen.taskapp2

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Task: RealmObject(), Serializable  {
    var title: String =""
    var category : String =""      /* added for Kadai */
    var contents: String =""
    var date:Date = Date()

    //idをプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}