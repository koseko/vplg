<?php session_start(); ?>
<!DOCTYPE html>
<?php 
/** 
 * @author Tim Schäfer
 */
include('./backend/config.php');
$SHOW_ERROR_LIST = array();
include('./common.php');
$DO_SHOW_ERROR_LIST = $DEBUG_MODE;
$SHOW_ERROR_LIST = array();

function run_plcc_visualization($gml_file, $markings_file) {
    $result_image_path = "outimage.png";
	return $result_image_path;
}


function get_protein_graph_file_name_no_ext($pdbid, $chain, $graphtype_string) {
  return $pdbid . "_" . $chain . "_" . $graphtype_string . "_PG";
}


function get_protein_graph_path_and_file_name_no_ext($pdbid, $chain, $graphtype_string) {
  $path = get_path_to($pdbid, $chain);
  $fname = get_protein_graph_file_name_no_ext($pdbid, $chain, $graphtype_string);
  return $path . $fname;
}


function get_graphtype_string($graphtype_int){
	switch ($graphtype_int){
		case 1:
			return "alpha";
			break;
		case 2:
			return "beta";
			break;
		case 3:
			return "albe";
			break;
		case 4:
			return "alphalig";
			break;
		case 5:
			return "betalig";
			break;
		case 6:
			return "albelig";
			break;
	}
}

function check_valid_pdbid($str) {
  if (preg_match('/^[A-Z0-9]{4}$/i', $str)) {
    return true;
  }  
  return false;
}

function check_valid_chainid($str) {
  if (preg_match('/^[A-Z0-9]{1}$/i', $str)) {
    return true;
  }  
  return false;
}

function get_folding_graph_file_name_no_ext($pdbid, $chain, $graphtype_string, $fg_number) {
  return $pdbid . "_" . $chain . "_" . $graphtype_string . "_FG_" . $fg_number;
}

function get_path_to($pdbid, $chain) {
  $mid2chars = substr($pdbid, 1, 2);
  return $mid2chars . "/" . $pdbid . "/". $chain . "/";
}

function get_folding_graph_path_and_file_name_no_ext($pdbid, $chain, $graphtype_string, $fg_number) {
  $path = get_path_to($pdbid, $chain);
  $fname = get_folding_graph_file_name_no_ext($pdbid, $chain, $graphtype_string, $fg_number);
  return $path . $fname;
}


// check for file name like '7tim_A_albe_PG.gml'
function check_valid_plcc_PG_gmlfile_name($str) {
  if (preg_match('/^[A-Za-z0-9]{4}_[A-Za-z0-9]{1}_(alpha|beta|albe|alphalig|betalig|albelig)_PG.gml$/i', $str)) {
    return true;
  }  
  return false;
}

$title = "BKweb -- Visualize substructure comparison";
$title = $SITE_TITLE.$TITLE_SPACER.$title;
?>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="PTGL folding graphs">
	<meta name="author" content="">
	<meta http-equiv="Cache-control" content="public">
	<link rel="shortcut icon" href="favicon.ico?v=1.0" type="image/x-icon" />

	<title><?php echo $title; ?></title>

	<!-- Mobile viewport optimized -->
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scale=1.0, user-scalable=no"/>

	<!-- Bootstrap CSS -->
	<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="css/bootstrap-glyphicons.css">

	<!-- Custom CSS -->
	<link rel="stylesheet" type="text/css" href="css/styles.css">
	<link rel="stylesheet" href="css/font-awesome.css"/>

	<script src="//ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
	<!-- Include Modernizr in the head, before any other JS -->
	<script src="js/modernizr-2.6.2.min.js"></script>

	<!-- Live Search for PDB IDs -->
	<script src="js/livesearch.js" type="text/javascript"></script>
</head>
<body id="customBackground">
	<noscript>
		<META HTTP-EQUIV="Refresh" CONTENT="0;URL=errorJS.php">
	</noscript>
	<div class="wrapper">

	<?php include('navbar.php'); ?>

	<div class="container" id="publications">
		<h2>Protein ligand graph comparison</h2>
		<br>
		
		<div id="PageIntro">
		<div class="container" id="pageintro">
		This service allows you to visualize substructures of graphs.
		
		</div><!-- end container-->
		</div><!-- end Home -->
		
		
		<?php
		$valid_values = FALSE;
		$pageload_was_search = FALSE;

		if(isset($_POST['first_pdbchain']) && isset($_POST['first_graphtype_int']) &&  isset($_POST['second_pdbchain']) && isset($_POST['second_graphtype_int']) && isset($_POST['result_id'])){
			$pageload_was_search = TRUE;
			$valid_values = FALSE;
			
			$first_pdbchain = $_POST["first_pdbchain"];
			$second_pdbchain = $_POST["second_pdbchain"];
			$first_graphtype_int = $_POST["first_graphtype_int"];
			$second_graphtype_int = $_POST["second_graphtype_int"];
			$int_result_id = intval($_POST['result_id']);

			$valid_values_first = FALSE;
			if($first_graphtype_int === "1" || $first_graphtype_int === "2" || $first_graphtype_int === "3" || 
				$first_graphtype_int === "4" || $first_graphtype_int === "5" || $first_graphtype_int === "6") 
				{ 

					if(strlen($first_pdbchain) === 5) {
						$first_pdb_id = substr($first_pdbchain, 0, 4);
						$first_chain_name = substr($first_pdbchain, 4, 1);						
					
						if(check_valid_pdbid($first_pdb_id) && check_valid_chainid($first_chain_name)) {
							$valid_values_first = TRUE;				  
						}
					}					
			}
			
			$valid_values_second = FALSE;
			if($second_graphtype_int === "1" || $second_graphtype_int === "2" || $second_graphtype_int === "3" || 
				$second_graphtype_int === "4" || $second_graphtype_int === "5" || $second_graphtype_int === "6") 
				{ 

					if(strlen($second_pdbchain) === 5) {
						$second_pdb_id = substr($second_pdbchain, 0, 4);
						$second_chain_name = substr($second_pdbchain, 4, 1);						
					
						if(check_valid_pdbid($second_pdb_id) && check_valid_chainid($second_chain_name)) {
							$valid_values_second = TRUE;				  
						}
					}					
			}
			
			$valid_values = ($valid_values_first && $valid_values_second);
		}
		
		if($valid_values) {
		    echo '<h3>OK</h3>';
		    $first_graphtype_str = get_graphtype_string($first_graphtype_int);	
		    $first_graph_file_name_no_ext = get_protein_graph_path_and_file_name_no_ext($first_pdb_id, $first_chain_name, $first_graphtype_str);
		    $first_full_file = $IMG_ROOT_PATH . $first_graph_file_name_no_ext . ".gml";
	

			$second_graphtype_str = get_graphtype_string($second_graphtype_int);	
			$second_graph_file_name_no_ext = get_protein_graph_path_and_file_name_no_ext($second_pdb_id, $second_chain_name, $second_graphtype_str);
			$second_full_file = $IMG_ROOT_PATH . $second_graph_file_name_no_ext . ".gml";
		
			if(file_exists($first_full_file) && file_exists($second_full_file)){
				$gml_files_available = TRUE;
				echo "GML files found";
			}
			else {
				echo "No GML files found.";
			}
		}		
		else {
		    echo '<h3>Invalid request.</h3><p>If you came here via a link on the PTGL website, please report this bug.</p>';
		}
		?>
		
		
		
		<br><br>
		
		<div class="container" id="graphImage">
			
			
		</div><!-- end container and searchResults -->
							

</div><!-- end container and contentText -->
</div><!-- end wrapper -->



<?php include('footer.php'); ?>
	<!-- All Javascript at the bottom of the page for faster page loading -->
	<!-- also needed for the dropdown menus etc. ... -->

	<!-- First try for the online version of jQuery-->
	<script src="http://code.jquery.com/jquery.js"></script>

	<!-- If no online access, fallback to our hardcoded version of jQuery -->
	<script>window.jQuery || document.write('<script src="js/jquery-1.8.2.min.js"><\/script>')</script>

	<!-- Bootstrap JS -->
	<script src="js/bootstrap.min.js"></script>

	<!-- Custom JS -->
	<script src="js/script.js"></script>
</body>
</html>