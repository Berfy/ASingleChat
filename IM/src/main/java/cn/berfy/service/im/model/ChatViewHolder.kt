package cn.berfy.service.im.model

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView

import cn.berfy.service.im.R

/**
 * 聊天界面的viewHolder
 *
 * @author NorthStar
 * @date 2019/1/21 16:47
 */
class ChatViewHolder(itemView: View) {
    var position: Int = 0
    var messages: List<Message>? = null
    var leftMessage: RelativeLayout = itemView.findViewById(R.id.im_leftMessage)
    var rightMessage: RelativeLayout = itemView.findViewById(R.id.im_rightMessage)
    var leftPanel: RelativeLayout = itemView.findViewById(R.id.im_leftPanel)
    var rightPanel: RelativeLayout = itemView.findViewById(R.id.im_rightPanel)
    var sending: ProgressBar = itemView.findViewById(R.id.im_sending)
    var ivReceiver: ImageView = itemView.findViewById(R.id.im_leftAvatar)
    var ivSender: ImageView = itemView.findViewById(R.id.im_rightAvatar)
    var error: ImageView = itemView.findViewById(R.id.im_sendError)
    var leftSender: TextView = itemView.findViewById(R.id.im_left_sender)
    var rightSender: TextView = itemView.findViewById(R.id.im_right_sender)
    var timeMessage: TextView = itemView.findViewById(R.id.im_timeMessage)
    var rightDesc: TextView = itemView.findViewById(R.id.im_rightDesc)
}
