<?php
/** This file retrieves ligand-centered complex graphs from the database, based on a query for a PDB ID.
 * 
 * @author Tim Schäfer
 */

ini_set('display_errors', 0);
ini_set('display_startup_errors', 0);
ini_set('log_errors', TRUE);
error_reporting(E_ERROR);

$valid_values = FALSE;
// get config values
include('./backend/config.php'); 

if($DEBUG){
	ini_set('display_errors', 1);
	ini_set('display_startup_errors', 1);
	ini_set('log_errors', TRUE);
	error_reporting(E_ALL);
}

function get_ligand_expo_link($ligand_name3) {
    if(strlen($ligand_name3) === 3 || strlen($ligand_name3) === 2 || strlen($ligand_name3) === 1) {
      return "http://ligand-expo.rcsb.org/pyapps/ldHandler.py?formid=cc-index-search&target=" . $ligand_name3 . "&operation=ccid";
    }
    return false;    
}


function get_ligandcomplexgraph_query_string($pdb_id) {
   $query = "SELECT lcg.ligandcenteredgraph_id, lcg.pdb_id, lcg.lig_sse_id, lcg.filepath_lcg_png, lcg.filepath_lcg_svg, lcg.filepath_lcg_pdf FROM plcc_ligandcenteredgraph lcg WHERE ( lcg.pdb_id = '" . $pdb_id . "' ) ORDER BY lcg.lig_sse_id";
   return $query;
}

function get_contact_chains_for_ligand($db, $lig_sse_db_id) {
  $lig_contact_chains = array();
  $query = "SELECT c.chain_name, lcg2c.lcg2c_ligandcenteredgraph_id, lcg2c.lcg2c_chain_id FROM plcc_nm_lcgtochain lcg2c INNER JOIN plcc_chain c ON lcg2c.lcg2c_chain_id = c.chain_id INNER JOIN plcc_ligandcenteredgraph lcg ON lcg2c.lcg2c_ligandcenteredgraph_id = lcg.ligandcenteredgraph_id WHERE ( lcg.lig_sse_id = " . $lig_sse_db_id . " );";
  $result = pg_query($db, $query);
  while ($arr = pg_fetch_array($result, NULL, PGSQL_ASSOC)){
    array_push($lig_contact_chains, $arr['chain_name']);
  }
  return $lig_contact_chains;
}

function get_lcg_ids_for_ligand($db, $lig_sse_db_id) {
  $lcg_ids = array();
  $query = "SELECT lcg.ligandcenteredgraph_id FROM plcc_ligandcenteredgraph lcg  WHERE ( lcg.lig_sse_id = " . $lig_sse_db_id . " );";
  $result = pg_query($db, $query);
  while ($arr = pg_fetch_array($result, NULL, PGSQL_ASSOC)){
    array_push($lcg_ids, $arr['ligandcenteredgraph_id']);
  }
  return $lcg_ids;
}


function get_all_chains_of_pdb_query($pdb_id) {
  $query = "SELECT c.chain_id, c.chain_name, c.organism_scientific, c.mol_name FROM plcc_chain c WHERE ( c.pdb_id = '" . $pdb_id . "' )";
  return $query;
}

function get_all_ligands_of_pdb_query($pdb_id) {
  $query = "SELECT s.sse_id, s.lig_name, s.pdb_start, s.pdb_end, s.dssp_start, s.dssp_end, c.chain_name, c.pdb_id FROM plcc_sse s INNER JOIN plcc_chain c ON s.chain_id = c.chain_id WHERE ( c.pdb_id = '" . $pdb_id . "' AND s.sse_type = 3 ) ORDER BY s.dssp_start";
  return $query;
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


function get_complex_sse_graph_file_name_no_ext($pdbid, $graphtype_string) {
  return $pdbid . "_complex_sses_" . $graphtype_string . "_CG";
}

function get_complex_chains_graph_file_name_no_ext($pdbid, $graphtype_string) {
  return $pdbid . "_complex_chains_CG";
}

function get_path_to($pdbid, $chain) {
  $mid2chars = substr($pdbid, 1, 2);
  return $mid2chars . "/" . $pdbid . "/". $chain . "/";
}

function get_complex_sse_graph_path_and_file_name_no_ext($pdbid, $graphtype_string) {
  $path = get_path_to($pdbid, $chain);
  $fname = get_complex_sse_graph_file_name_no_ext($pdbid, $graphtype_string);
  return $path . $fname;
}

function get_complex_chains_graph_path_and_file_name_no_ext($pdbid, $graphtype_string) {
  $path = get_path_to($pdbid, $chain);
  $fname = get_complex_chains_graph_file_name_no_ext($pdbid, $graphtype_string);
  return $path . $fname;
}

$pageload_was_search = FALSE;
$valid_values = FALSE;

if(isset($_GET['pdb'])){
        
        $pageload_was_search = TRUE;
	$valid_values = FALSE;
	$pdb_id = $_GET["pdb"];
	if(check_valid_pdbid($pdb_id)) {
		$valid_values = TRUE;				  
	}
}

$num_found = 0;
$num_lig_found = 0;
$num_lig_with_lcg = 0;
$num_lig_nolcg_found = 0;
$no_lcg_table_string = "";

if($valid_values){
    //echo "valid";
	// connect to DB
	$conn_string = "host=" . $DB_HOST . " port=" . $DB_PORT . " dbname=" . $DB_NAME . " user=" . $DB_USER ." password=" . $DB_PASSWORD;
	$db = pg_connect($conn_string);
	if(! $db) { array_push($SHOW_ERROR_LIST, "Database connection failed."); }
	//if(! $db) { echo "NO_DB"; }
		
	// determine all chains
	$chains_query = get_all_chains_of_pdb_query($pdb_id);
	$chains_result = pg_query($db, $chains_query);
	$chains = array();
	$tableString = "<div><table id='tblfgresults'><tr><th>PDB ID</th><th>Chain</th><th>Molecule</th><th>Organism</th><th>Go to protein graph</th></tr>\n";
	while ($chains_arr = pg_fetch_array($chains_result, NULL, PGSQL_ASSOC)){
		// data from chains table:
	        $cg_chain_name = $chains_arr['chain_name'];
	        $mol_name = $chains_arr['mol_name'];
	        $organism = $chains_arr['organism_scientific'];
	        $db_chain_id = $chains_arr['chain_id'];
	        array_push($chains, $cg_chain_name);
	        $pdbchain = $pdb_id . $cg_chain_name;
	        $tableString .= "<tr>\n";
		$tableString .= "<td>$pdb_id</td><td>$cg_chain_name</td><td>$mol_name</td><td>$organism</td>";
		$tableString .= "<td><a href='./results.php?q=" . $pdbchain . "' alt='Show protein graph of this chain'>PG of " . $pdb_id . " chain " . $cg_chain_name . "</a></td>\n";
		$tableString .= "</tr>\n";
		
	}
	$tableString .= "</table></div>\n";
	
	
	// determine all ligands (single ligand molecules from SSEs (ligand residues, not ligand types. This means something like ICT-485, not ICT)
	$ligands_query = get_all_ligands_of_pdb_query($pdb_id);
	$ligands_result = pg_query($db, $ligands_query);
	$ligtableString = "<div><table id='tblligresults' class='results'><tr><th>Ligand index</th><th>Ligand type</th><th>Chain</th><th>1st PDB residue</th><th>1st DSSP residue</th><th>Contact chains</th><th>Go to ligand complex graph</th></tr>\n";
	
	$ligand_names = array();
	$ligand_chains = array(); // the chain a ligand is assigned to (in PDB/DSSP data), by index
	$lig_indices_by_sse_id = array();
	$ligand_pdb_start = array();
	$ligand_dssp_start = array();
	
	$no_lcg_indices = array();	// indices of all ligands which do not have a dedicated LCG, because they have contacts only to their own chain
	
	$lig_idx = 0;
	while ($ligand_arr = pg_fetch_array($ligands_result, NULL, PGSQL_ASSOC)){
		// data from SSE table (not from ligands table):
	        $lig_name = $ligand_arr['lig_name'];
	        $lig_chain_name = $ligand_arr['chain_name'];
	        $lig_pdb_start = $ligand_arr['pdb_start'];
	        $lig_pdb_end = $ligand_arr['pdb_end'];
	        $lig_dssp_start = $ligand_arr['dssp_start'];
	        $lig_dssp_end = $ligand_arr['dssp_end'];
	        $lig_sse_db_id = $ligand_arr['sse_id'];
	        $num_lig_found++;
	        
	        $lig_name = trim($lig_name);
	        
	        $tag_name = "ligand" . $lig_idx;
	        $ligand_names[$lig_idx] = $lig_name;
	        $ligand_chains[$lig_idx] = $lig_chain_name;
	        $ligand_pdb_start[$lig_idx] = $lig_pdb_start;
	        $ligand_dssp_start[$lig_idx] = $lig_dssp_start;
	        $lig_indices_by_sse_id[$lig_sse_db_id] = $lig_idx;
	        
	        
                $ligexpo_link = get_ligand_expo_link($lig_name);
		if($ligexpo_link) {
		    $lig_name_link = "<a href='" . $ligexpo_link . "' target='_blank'>" . $lig_name . "</a>";
		}
		else {
		    $lig_name_link = $lig_name;
		}
		
		$contact_chains = get_contact_chains_for_ligand($db, $lig_sse_db_id);
		$lcg_ids = get_lcg_ids_for_ligand($db, $lig_sse_db_id);		
		$lig_has_lcg = (count($lcg_ids) >= 1);
	        
	        $ligtableString .= "<tr>\n";
		$ligtableString .= "<td>$lig_idx</td><td>$lig_name_link</td><td>$lig_chain_name</td><td>$lig_pdb_start</td><td>$lig_dssp_start</td>";
		
		if($lig_has_lcg) {
		    $num_lig_with_lcg++;
		    $ligtableString .= "<td>";
		    for($i = 0; $i < count($contact_chains); $i++) {
		      $ligtableString .= "" . $contact_chains[$i];
		      if($i < (count($contact_chains) - 1)) {
		        $ligtableString .= ", ";
		      }
		    }
		} else {
		    array_push($no_lcg_indices, $lig_idx);
		    $ligtableString .= "<td>" . $lig_chain_name . "</td>\n";
		}
		
		
		if($lig_has_lcg) {
		    $ligtableString .= "<td><a href='#" . $tag_name . "' alt='Show ligand complex graphs for this ligand'>LCG of ligand " . $lig_name ." (PDB residue " . $lig_pdb_start . ")</a></td>\n";
		} else {
		    $ligtableString .= "<td><a href='./results.php?q=" . $pdb_id . $lig_chain_name ."' alt='Show protein graph for chain of this ligand'>PG of the ligand chain " . $lig_chain_name . "</a></td>\n";
		}
		
		$ligtableString .= "</tr>\n";
		$lig_idx++;
		
	}
	$ligtableString .= "</table></div>\n";
	
	
	
	$num_lig_nolcg_found = count($no_lcg_indices);
	if($num_lig_nolcg_found > 0) {
	  $no_lcg_table_string = "<br><br><h4>Ligands which only have contacts to their own chain</h4>";
	  $no_lcg_table_string .= "<p>These $num_lig_nolcg_found ligands do not need a special ligand-centered graph, use the albelig protein graph instead.</p>";
	  
	  // print HTML jump labels (these are for users entering the page with a link that includes the ligand index)
	  foreach($no_lcg_indices as $ligand_index) {
	    $no_lcg_table_string .= "<a name='#ligand" . $ligand_index . "></a>\n";
	  }
	  
	  $no_lcg_table_string .= "<table class='results'><tr><th>Ligand index</th><th>Name</th><th>Chain</th><th>Contact chains</th><th>Link</th></tr>\n";
	  foreach($no_lcg_indices as $ligand_index) {
	    $ch = $ligand_chains[$ligand_index];
	    $no_lcg_table_string .= "<tr><td>" . $ligand_index . "</td><td>" . $ligand_names[$ligand_index] . "</td><td>". $ch . "</td><td>" . $ch . "</td><td>" . "<a href='results.php?q=" . $pdb_id . $ch . "'>PG of " . $pdb_id . " chain " . $ch . "</a></td></tr>\n";
	  }
	  $no_lcg_table_string .= "</table>\n";
	}
	
	
	$query = get_ligandcomplexgraph_query_string($pdb_id);
	
	//echo "query='" . $query . "'\n";
	
	$result = pg_query($db, $query);
	if(! $result) { array_push($SHOW_ERROR_LIST, "Database query failed: '" . pg_last_error($db) . "'"); }
		
	$num_found = 0;
	$img_string = "";
	$html_id = "<br><br><br><h4> Ligand-centered graphs</h4>";
	while ($arr = pg_fetch_array($result, NULL, PGSQL_ASSOC)){
		// data from complexgraph table:
	        $lcg_id = $arr['ligandcenteredgraph_id'];	
	        $lcg_pdb_id = $arr['pdb_id'];
	        $lcg_lig_sse_id = $arr['lig_sse_id'];
		$sse_img_png = $arr['filepath_lcg_png']; // the PNG format image
		$sse_img_pdf = $arr['filepath_lcg_pdf'];
		$sse_img_svg = $arr['filepath_lcg_svg'];


		// ------------------------------------- handle ligand-centered complex graph images ---------------------------
		$sse_image_exists_png = FALSE;
		$sse_img_link = "";
		$full_sse_img_path_png = $IMG_ROOT_PATH . $sse_img_png;
		if(isset($sse_img_png) && $sse_img_png != "" && file_exists($full_sse_img_path_png)) {
			$sse_image_exists_png = TRUE;
		} else {
		    //echo "File '$full_img_path_png' does not exist.";
		}
		
		$sse_image_exists_pdf = FALSE;
		$full_sse_img_path_pdf = $IMG_ROOT_PATH . $sse_img_pdf;
		if(isset($sse_img_pdf) && $sse_img_pdf != "" && file_exists($full_sse_img_path_pdf)) {
			$sse_image_exists_pdf = TRUE;
		}
		
		$sse_image_exists_svg = FALSE;
		$full_sse_img_path_svg = $IMG_ROOT_PATH . $sse_img_svg;
		if(isset($sse_img_svg) && $sse_img_svg != "" && file_exists($full_sse_img_path_svg)) {
			$sse_image_exists_svg = TRUE;
		}
				
		// prepare the image links
		$this_lig_idx = $lig_indices_by_sse_id[$lcg_lig_sse_id];
		$this_lig_name3 = $ligand_names[$this_lig_idx];
		$this_lig_chain = $ligand_chains[$this_lig_idx];
		$this_lig_pdb_start = $ligand_pdb_start[$this_lig_idx];
		$this_lig_dssp_start = $ligand_dssp_start[$this_lig_idx];
		
		$img_string .= "<br><br><br><h4> Ligand #" . $this_lig_idx . ": " . $this_lig_name3 . "</h4><br>\n";
		
		if($sse_image_exists_png) {		    
		    
		    $img_string .= "<div id='sse_cg'><a name='ligand" . $this_lig_idx . "'></a><p>Graph for PDB " . $lcg_pdb_id . " ligand no. " . $this_lig_idx . " (PDB residue $this_lig_pdb_start of chain $this_lig_chain):</p><img src='" . $full_sse_img_path_png . "' width='800'></div><br><br>\n";
		} else {
		    $img_string .= "<b>Image not available:</b> <i>The ligand-centered complex graph for the ligand # $this_lig_idx (PDB residue $this_lig_pdb_start of chain $this_lig_chain) is not available, sorry.</i>";
		}
		
		// add download links for other formats than PNG (they can directly d/l this from the browser image)
		if($sse_image_exists_svg || $sse_image_exists_pdf || $sse_image_exists_png) {
		  $img_string .= "Download the visualization in formats: ";
		  
		  if($sse_image_exists_png) {
		    $img_string .= ' <a href="' . $full_sse_img_path_png .'" target="_blank">[PNG]</a>';
		  }
		  
		  if($sse_image_exists_svg) {
		    $img_string .= ' <a href="' . $full_sse_img_path_svg .'" target="_blank">[SVG]</a>';
		  }
		  
		  if($sse_image_exists_pdf) {
		    $img_string .= ' <a href="' . $full_sse_img_path_pdf .'" target="_blank">[PDF]</a>';
		  }
		  $img_string .= "<br/>";
		  
		}
		
		$num_found++;
				
	}		
	
	
	
	
	


} else {
     //echo "invalid";
	$tableString = "";
}

//EOF
?>