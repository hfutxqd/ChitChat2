<?php
define("APP_PATH",dirname(__FILE__));
define("SP_PATH",dirname(__FILE__).'/SpeedPHP');
$spConfig = array(
	"db" => array(
		'host' => '127.0.0.1',
		'login' => 'root',
		'password' => '19940625',
		'database' => 'ChitChatExplore',
	),
	'mode' => 'debug',
	'view' => array(
		'enabled' => true,
		'config' =>array(
			'template_dir' => APP_PATH.'/tpl', 
			'compile_dir' => APP_PATH.'/tmp',
			'cache_dir' => APP_PATH.'/tmp', 
			'left_delimiter' => '<{',  
			'right_delimiter' => '}>', 
		)
	),
	'launch' => array(
	 'router_prefilter' => array( 
		array('spAcl','mincheck')
	 	)
	 ),
	'include_path'=>array( APP_PATH.'/php',),
);
require(SP_PATH."/SpeedPHP.php");
spRun();
?>