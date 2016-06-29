<?php
header('Content-type:application/json;charset=UTF-8');
import("uploadFile.php");
import("ServerAPI.php");

class main extends spController
{
    public $BASE_URL = 'http://139.129.53.121/';
    public $server;

    /**
     * main constructor.
     * @param ServerAPI $server
     */
    public function __construct()
    {
        $this->server = new ServerAPI('x18ywvqf873kc','YIqsj1MJDi9o4', '10000');
    }
    
    function index(){
//        $this->server->sendCommentMessage('66', 'f21326cb83e19ca8', 'Hello world!', '');
    }
    //input示例
    //{
    //"nickname":"徐啟东",
    //"device_id":"b952ebcba46f7fad",
    //"content":{
    //  "text": "哈哈哈哈哈哈",
    //  "images":["http://localhost/ChitChatExplore/attached/201606081320275506.jpg"]
    //}
    //}
    function publish()
    {
        $data = file_get_contents('php://input');
        $ob = json_decode($data, true);
        $id = spClass("explore")->add($ob);
        $rtn['success'] = true;
        $rtn['id'] = $id;
        echo json_encode($rtn);
    }

    function comment()
    {
        $data = file_get_contents('php://input');
        $ob = json_decode($data, true);
        spClass("comment")->add($ob);
        $explore = spClass("explore")->find('id = '.$ob['explore_id']);
        $toUsers = spClass("comment")->findSql('SELECT DISTINCT `device_id` FROM `comment` WHERE explore_id = '.$ob['explore_id']);

        $toUserArr = array();
        foreach ($toUsers as $item)
        {
            array_push($toUserArr, $item['device_id']);
        }
        array_push($toUserArr, $explore['device_id']);
//        $push_content['type'] = 'comment';
//        $push_content['explore_id'] = $explore['id'];
//        $push_content['content'] = $ob['text'];
//        $push_content['nickname'] = $ob['nickname'];
//        $push_content['extra'] = '';
//        $this->server->sendTxtMessage($toUserArr, json_encode($push_content), $push_content);
        $this->server->sendCommentMessage($explore['id'], $ob['device_id'], $toUserArr, $ob['text'], '');
        $rtn['success'] = true;
        echo json_encode($rtn);
    }

    function like()
    {
        $data = file_get_contents('php://input');
        $ob = json_decode($data, true);
        $id = spClass('like')->add($ob);
        $rtn['success'] = true;
        $rtn['id'] = $id;
        echo json_encode($rtn);
    }

    function unlike()
    {
        $data = file_get_contents('php://input');
        $ob = json_decode($data, true);
        $res = spClass('like')->remove($ob);
        $rtn['success'] = $res;
        echo json_encode($rtn);
    }

    function explore()
    {
        $explore = spClass("explore");
        $id = $this->spArgs('id', 0);
        $device_id = $this->spArgs('device_id', 0);
        $data = $explore->find("id = '$id'");
        $data['content'] = json_decode($data['content']);
        $res = $explore->findSql('SELECT * FROM `likes` WHERE `device_id` = "'.$device_id.'" AND `explore_id` = '.$id.'; ');
        $data['isLiked'] = ($res != null);
        echo json_encode($data);
    }

    function ListExplore()
    {
        $explore = spClass("explore");
        $id = $this->spArgs('id', 0);
        $device_id = $this->spArgs('device_id', 0);
        if($id == 0)
        {
            $data = $explore->findSql('SELECT * FROM `explore` ORDER BY `id` DESC LIMIT 10;');
        }else{
            $data = $explore->findSql('SELECT * FROM `explore` WHERE id < '.$id.' ORDER BY `id` DESC LIMIT 10;');
        }
        foreach ($data as $key => $value)
        {
            $data[$key]['content'] = json_decode($data[$key]['content']);
            $res = $explore->findSql('SELECT * FROM `likes` WHERE `device_id` = "'.$device_id.'" AND `explore_id` = '.$data[$key]['id'].'; ');
            $data[$key]['isLiked'] = ($res != null);
        }
        echo json_encode($data);
    }

    function ListComment()
    {
        $comment = spClass("comment");
        $id = $this->spArgs('id');
        $data = $comment->findSql("SELECT * FROM `comment` WHERE explore_id = $id ORDER BY `time` DESC");
        echo json_encode($data);
    }

    function upload()
    {
        $file = new uploadFile();
        $res = $file->upload_file($_FILES['image']);
        if(!$res)
        {
            $rtn['success'] = false;
            $rtn['message'] = $file->errmsg;
            echo json_encode($rtn);
            return;
        }else{
            $rtn['success'] = true;
            $rtn['url'] = $this->BASE_URL.$file->uploaded;
            echo json_encode($rtn);
        }
    }
}
?>