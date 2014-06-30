<?php

use ZipArchive;

ini_set('display_errors',1);
ini_set('display_startup_errors',1);
error_reporting(-1);

function create_zip($files = array(),$destination = '',$overwrite = false) {
	//if the zip file already exists and overwrite is false, return false
	$destination = "./temp_downloads/".$destination;
	if(file_exists($destination) && !$overwrite) { return false; }
	//vars
	$valid_files = array();
	//if files were passed in...
	if(is_array($files)) {
		//cycle through each file
		foreach($files as $file) {
			//make sure the file exists
			if(file_exists('../data/'.$file)) {
				$valid_files[] = $file;
			}
		}
	}
	//if we have good files...
	if(count($valid_files)) {
		//create the archive
		$zip = new ZipArchive();
		if($zip->open($destination,$overwrite ? ZIPARCHIVE::OVERWRITE : ZIPARCHIVE::CREATE) !== true) {
			return false;
		}
		//add the files
		var_dump($valid_files);
		foreach($valid_files as $file) {
			$zip->addFile($file,$file);
		}
		//debug
		//echo 'The zip archive contains ',$zip->numFiles,' files with a status of ',$zip->status;
		
		//close the zip -- done!
		$zip->close();
		if(file_exists($destination)){
			header("Content-type: application/zip"); 
			header("Content-Disposition: attachment; filename=$destination");
			header("Content-length: " . filesize($destination));
			header("Pragma: no-cache"); 
			header("Expires: 0"); 
			readfile("$destination");
		}
		//check to make sure the file exists
		return file_exists($destination);
	}
	else
	{
		return false;
	}
}




if(isset($_POST['proteins']) && isset($_POST['downloadType'])) {
	$proteins = $_POST['proteins'];
	$downloadType = $_POST['downloadType'];
	$stamp = time();
	$imageType = "graph_image_".strtolower($downloadType);
	
	$db_config = include('config.php'); 
	$conn_string = "host=" . $db_config['host'] . " port=" . $db_config['port'] . " dbname=" . $db_config['db'] . " user=" . $db_config['user'] ." password=" . $db_config['pw'];
	$db = pg_connect($conn_string)
			or die($db_config['db'] . ' -> Connection error: ' . pg_last_error() . pg_result_error() . pg_result_error_field() . pg_result_status() . pg_connection_status() );            

	pg_query($db, "DEALLOCATE ALL");
	$query = "SELECT * FROM plcc_chain, plcc_graph WHERE pdb_id=$1 AND chain_name LIKE $2 AND plcc_graph.chain_id = plcc_chain.chain_id ORDER BY graph_type"; 
	pg_prepare($db, "getImages", $query) or die($query . ' -> Query failed: ' . pg_last_error());		
	
	$files = array();
	foreach($proteins as $prot){
		$pdbID = substr($prot, 0, 4);
		$chain = substr($prot, 4, 1);

		$result = pg_execute($db, "getImages", array($pdbID, $chain)); 
		while ($arr = pg_fetch_array($result, NULL, PGSQL_ASSOC)){
			$files[] = $arr[$imageType];
		}
		
	}

	create_zip($files, 'PTGL_FILES_'.$downloadType."_".$stamp);

} else {
	echo "<script> alert('Error!');</script>";
}

?>