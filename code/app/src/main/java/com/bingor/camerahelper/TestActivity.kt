package com.bingor.camerahelper

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.bingor.camerahelperlib.view.ClickRollView
import com.bingor.utillib.log.Log

/**
 * Created by Bingor on 2019/2/19.
 */
class TestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val click = MyClick()
        findViewById<Button>(R.id.bt_test1).setOnClickListener(click)
        var xxxvTest = findViewById<ClickRollView>(R.id.xxxv_test)
        var datas = listOf<String>("aa", "bb", "cc")
        xxxvTest.setDatas(datas)
    }


    fun checkAllNotNull(vararg args: Any?): Boolean {
        return args.all { it != null }
    }

    fun test(paramA: Object?, paramB: Object?) {
        Log.d(paramA.toString())
        if (paramA != null && paramB != null) {
            Log.d(paramA.toString())
        }


        paramA?.let {
            paramB.let {
                //用it(即paramB)进行一些操作
                //同时，用paramA进行一些操作，但是这里it已经指代paramB了
                //那么，就以为着我需要用另一种方法来做这个let判断，让paramA、paramB都有别名
                //请问，有办法么？
            }
        }


    }

    fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
        return if (p1 != null && p2 != null) block(p1, p2) else null
    }


    inner class MyClick : View.OnClickListener {
        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.bt_test1 -> {
//                    var res = checkAllNotNull(1, false, null)
//                    Log.d("res==" + res)

                    val a = "string".let {
                        Log.d("res==" + it)
                        3
                    }

                    Log.d("res2==" + a)
                }
            }
        }
    }
}