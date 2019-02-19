package cn.berfy.service.im.manager.i

/**
author: Berfy
date: 2019/1/24
 */
interface IMCallback {

    fun onSuc()
    fun onFailed(err: String)
}