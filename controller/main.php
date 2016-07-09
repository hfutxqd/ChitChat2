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
        $this->server = new ServerAPI('x18ywvqf873kc', 'YIqsj1MJDi9o4', '10000');
    }

    function index()
    {
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

    //删除数组中的一个元素
    function array_remove_value(&$arr, $var)
    {
        foreach ($arr as $key => $value) {
            if (is_array($value)) {
                $this->array_remove_value($arr[$key], $var);
            } else {
                $value = trim($value);
                if ($value == $var) {
                    unset($arr[$key]);
                } else {
                    $arr[$key] = $value;
                }
            }
        }
    }

    function comment()
    {
        $data = file_get_contents('php://input');
        $ob = json_decode($data, true);
        spClass("comment")->add($ob);
        $explore = spClass("explore")->find('id = ' . $ob['explore_id']);

        //查找所有评论过的人
        $toUsers = spClass("comment")->findSql('SELECT DISTINCT `device_id` FROM `comment` WHERE explore_id = ' . $ob['explore_id']);
        $toUserArr = array();
        foreach ($toUsers as $item) {
            array_push($toUserArr, $item['device_id']);
        }
        array_push($toUserArr, $explore['device_id']);
        //如果有重复,去除重复
        $toUserArr = array_unique($toUserArr);
        //去除当前评论者
        $this->array_remove_value($toUserArr, $ob['device_id']);
        //发送消息,如果为空则不发送
        if (!empty($toUserArr)) {
            $this->server->sendCommentMessage($explore['id'], $ob['device_id'], $ob['color'], $ob['nickname'], $toUserArr, $ob['text'], '');
        }
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
        $res = $explore->findSql('SELECT * FROM `likes` WHERE `device_id` = "' . $device_id . '" AND `explore_id` = ' . $id . '; ');
        $data['isLiked'] = ($res != null);
        echo json_encode($data);
    }

    function ListExplore()
    {
        $explore = spClass("explore");
        $id = $this->spArgs('id', 0);
        $device_id = $this->spArgs('device_id', 0);
        if ($id == 0) {
            $data = $explore->findSql('SELECT * FROM `explore` ORDER BY `id` DESC LIMIT 10;');
        } else {
            $data = $explore->findSql('SELECT * FROM `explore` WHERE id < ' . $id . ' ORDER BY `id` DESC LIMIT 10;');
        }
        foreach ($data as $key => $value) {
            $data[$key]['content'] = json_decode($data[$key]['content']);
            $res = $explore->findSql('SELECT * FROM `likes` WHERE `device_id` = "' . $device_id . '" AND `explore_id` = ' . $data[$key]['id'] . '; ');
            $data[$key]['isLiked'] = ($res != null);
        }
        echo json_encode($data);
    }

    function exploreByPager()
    {
        $explore = spClass("explore");
        $page = $this->spArgs('page', 1);
        $device_id = $this->spArgs('device_id', 0);
        $sql = "SELECT * FROM explore WHERE device_id = '$device_id' ORDER BY id DESC";
        $data = $explore->spPager($page, 10)->findSql($sql);
        foreach ($data as $key => $value) {
            $data[$key]['content'] = json_decode($data[$key]['content']);
            $res = $explore->findSql('SELECT * FROM `likes` WHERE `device_id` = "' . $device_id . '" AND `explore_id` = ' . $data[$key]['id'] . '; ');
            $data[$key]['isLiked'] = ($res != null);
        }
        $pager = $explore->spPager()->getPager();
        if (is_null($pager)) {
            $pager['total_page'] = 1;
        } else {
            unset($pager['page_size']);
            unset($pager['all_pages']);
            unset($pager['total_count']);
            unset($pager['first_page']);
            unset($pager['last_page']);
            unset($pager['prev_page']);
        }
        $res['pager'] = $pager;
        $res['data'] = $data;
        echo json_encode($res);
    }

    function ListExploreByPager()
    {
        $explore = spClass("explore");
        $page = $this->spArgs('page', 1);
        $device_id = $this->spArgs('device_id', 0);
        $longitude = $this->spArgs('longitude', 0);
        $latitude = $this->spArgs('latitude', 0);
        if($latitude == 0 && $longitude == 0) {
            $sql = "SELECT * FROM `explore` ORDER BY `id` DESC";
        } else{
            $sql = "SELECT * FROM explore ORDER BY getOrder(getDistance(latitude, longitude,$latitude, $longitude), getTime(time)) DESC";
        }
        $data = $explore->spPager($page, 10)->findSql($sql);
        foreach ($data as $key => $value) {
            $data[$key]['content'] = json_decode($data[$key]['content']);
            $res = $explore->findSql('SELECT * FROM `likes` WHERE `device_id` = "' . $device_id . '" AND `explore_id` = ' . $data[$key]['id'] . '; ');
            $data[$key]['isLiked'] = ($res != null);
        }
        $pager = $explore->spPager()->getPager();
        if (is_null($pager)) {
            $pager['total_page'] = 1;
        } else {
            unset($pager['page_size']);
            unset($pager['all_pages']);
            unset($pager['total_count']);
            unset($pager['first_page']);
            unset($pager['last_page']);
            unset($pager['prev_page']);
        }
        $res['pager'] = $pager;
        $res['data'] = $data;
        echo json_encode($res);
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
        if (!$res) {
            $rtn['success'] = false;
            $rtn['message'] = $file->errmsg;
            echo json_encode($rtn);
            return;
        } else {
            $rtn['success'] = true;
            $rtn['url'] = $this->BASE_URL . $file->uploaded;
            echo json_encode($rtn);
        }
    }
}

?>