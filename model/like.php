<?php

/**
 * Created by PhpStorm.
 * User: imxqd
 * Date: 2016/6/7
 * Time: 23:24
 */
class like extends spModel
{
    var $pk = "id"; // 数据表的主键
    var $table = "likes"; // 数据表的名称

    public function add($ob)
    {
        $explore_id = $ob['explore_id'];
        $device_id = $ob['device_id'];
        $this->runSql("insert into `likes` (explore_id, device_id) values ($explore_id, '$device_id');");
        return true;
    }

    public function remove($ob)
    {
        $explore_id = $ob['explore_id'];
        $device_id = $ob['device_id'];
        return $this->runSql("delete from `likes` where explore_id = $explore_id and device_id = '$device_id';");
    }
}