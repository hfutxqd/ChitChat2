<?php

class explore extends spModel
{
    var $pk = "id"; // 数据表的主键
    var $table = "explore"; // 数据表的名称

    public function add($ob)
    {
        $nickname = $ob['nickname'];
        $device_id = $ob['device_id'];
        $color = $ob['color'];
        $longitude = $ob['longitude'];
        $latitude = $ob['latitude'];
        $loctionAdrr = $ob['loctionAdrr'];
        $content = json_encode($ob['content']);
        $res = $this->create(array(
            'device_id' => "$device_id",
            'nickname' => "$nickname",
            'content' => "$content",
            'latitude' => $latitude,
            'longitude' => $longitude,
            'loctionAdrr' => $loctionAdrr,
            'color' => $color,
        ));
        return $res;
    }
}

?>