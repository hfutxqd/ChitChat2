<?php

/**
 * Created by PhpStorm.
 * User: imxqd
 * Date: 2016/6/7
 * Time: 23:24
 */
class comment extends spModel
{
    var $pk = "id"; // 数据表的主键
    var $table = "comment"; // 数据表的名称
    public function add($ob)
    {
        $explore_id = $ob['explore_id'];
        $nickname = $ob['nickname'];
        $device_id = $ob['device_id'];
        $text =  $ob['text'];
        $color = $ob['color'];
        $this->runSql("insert into comment (explore_id, device_id, nickname, text, color) values ($explore_id, '$device_id', '$nickname', '$text', $color);");
        return true;
    }
}