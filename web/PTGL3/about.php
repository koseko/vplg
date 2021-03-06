<!DOCTYPE html>
<?php 
include('./backend/config.php'); 

$title = "About";
$title = $SITE_TITLE.$TITLE_SPACER.$title;
?>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="">
	<meta http-equiv="Cache-control" content="public">
	<meta name="author" content="">
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
	<div class="container" id="about">
		<h2> About </h2>
		<h3> Table of contents </h3>
		<ul>
			<li class="noBullets"> <a href="#ptgl">What is <?php echo "$SITE_TITLE"; ?>?</a> </li>
			<li class="noBullets">
				<ul>
					<li class="noBullets">  <a href="#proteinGraph">Protein graphs</a> </li>
					<ul>
					    <li class="noBullets">  <a href="#contacts">Atom and SSE contacts</a> </li>
					    <li class="noBullets">  <a href="#graphTypes">Graph types</a> </li>
					</ul>
					<li class="noBullets">  <a href="#explainImages">Interpreting the graph images</a> </li>
					<li class="noBullets">  <a href="#exampleGraphTypes">An example for the graph types</a> </li>
					<li class="noBullets">
						<ul>
							<li class="noBullets">  <a href="#alphaGraph">Alpha</a> </li>
							<li class="noBullets">  <a href="#betaGraph">Beta</a> </li>
							<li class="noBullets">  <a href="#alphaBetaGraph">Alpha-Beta</a> </li>
							<li class="noBullets">  <a href="#alphaLigGraph">Alpha-Ligand</a> </li>
							<li class="noBullets">  <a href="#betaLigGraph">Beta-Ligand</a> </li>
							<li class="noBullets">  <a href="#alphaBetaLigGraph">Alpha-Beta-Ligand</a> </li>
						</ul>
						
					
					<li  class="noBullets">  <a href="#foldingGraph">Folding Graphs</a> </li>
					<li class="noBullets">  <a href="#linearNot">Linear Notation</a> </li>
					<ul>
						<li class="noBullets">  <a href="#adj">Adjacent notation</a> </li>
						<li class="noBullets">  <a href="#red">Reduced notation</a> </li>
						<li class="noBullets">  <a href="#key">Key notation</a> </li>
						<li class="noBullets">  <a href="#seq">Sequence notation</a> </li>
					</ul>
					</li>
									</ul>
			 <li class="noBullets"> <a href="#linking">Linking <?php echo "$SITE_TITLE"; ?></a> </li>
			 <li class="noBullets"> <a href="#api">The <?php echo "$SITE_TITLE"; ?> REST API</a> </li>
			</li>
		</ul>

		<br>
		<br>	
		<a class="anchor" id="ptgl"></a>
		<h3> What is <?php echo "$SITE_TITLE"; ?>? </h3>

		<p>
		<?php echo "$SITE_TITLE"; ?> is a web-based database application for the analysis protein topologies. It uses a graph-based model to describe the structure
		of protein chains on the super-secondary structure level. A protein graph is computed from the 3D atomic coordinates of a single chain in
		a PDB file and the secondary structure assignments of the DSSP algorithm. The computation of the protein graph is done by our software <a href="http://www.bioinformatik.uni-frankfurt.de/tools/vplg/" target="_blank">Visualization of Protein Ligand Graphs (VPLG)</a>. In a protein graph graph, vertices represent secondary
		structure elements (SSEs, usually alpha helices and beta strands) or ligand molecules while the edges model contacts and relative orientations between
		them. The result is an undirected, labelled graph for a single protein chain.
		<br /><br /></p>
		
		<p class="imgCenter"><img src="./images/how_vplg_works.png" alt="Protein graph computation" title="Protein graph computation" class="img-responsive imgFormAboutphp"/></p>
		
		<br/>
		
		<!--
		The most common and important SSEs in proteins are alpha helices and beta strands, and some structures in the PDB also contain ligands.
		So the PTGL contains 6 different graph types, which differ in the considered secondary structure elements (SSE). The three base graph types are the <a href="#alphaGraph">Alpha graph</a>, the <a href="#betaGraph">Beta graph</a>,
		and the <a href="#alphaBetaGraph">Alpha-Beta graph</a>. If you are interested in the ligands as well, you can also use the <a href="#alphaLigGraph">Alpha-Ligand graph</a>, the <a href="#betaLigGraph">Beta-Ligand graph</a>,
		and the <a href="#alphaBetaLigGraph">Alpha-Beta-Ligand graph</a>
		-->
		
		<!--
		The connected components of the <a href="#proteinGraph">Protein graph</a> form <a href="#foldingGraph">Folding graphs</a>. A <a href="#proteinGraph">Protein graph</a> can consist of one or more
		<a href="#foldingGraph">Folding graphs</a>. The three graph types were defined for each protein of the <a href="http://www.rcsb.org/pdb/" target="_blank">PDB</a>. For each graph type exists four <a href="#linearNot">linear notations</a> with
		corresponding graphic representations. In PTGL all <a href="#foldingGraph">Folding graphs</a>, all SSEs, and additional protein information are stored for every
		protein structure annotated in <a href="http://www.rcsb.org/pdb/" target="_blank">PDB</a> for which SSEs according DSSP are defined, which is not a NMR structure, has a resolution less than 3.5
		Å and a sequence length of at least 20 amino acids. The database enables the user to search for the topology of a protein or for certain
		topologies and subtopologies using the <a href="#linearNot">linear notations</a>. Additionally, it could be searched for sequence similarity in <a href="http://www.rcsb.org/pdb/" target="_blank">PDB</a> sequences.
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		-->

		<br>
		<a class="anchor" id="proteinGraph"></a>
		<h3> Protein Graphs </h3>

		<p>Using the 3D structure data from the <a href="http://www.rcsb.org/pdb/" target="_blank">PDB</a>, the SSEs are defined according to
		the assignment of the <a href="http://swift.cmbi.ru.nl/gv/dssp/" target="_blank">DSSP</a> algorithm with some 
		modifications. Then, the spatial
		contacts between the SSEs are computed according to <a href="publications.php">Koch <i>et al.</i>, 2013</a>. For the ligand versions, the explanation can
		be found in <a href="publications.php">Sch&auml;fer <i>et al.</i>, 2012</a>. This information forms the basis for the description of protein structures as
		graphs.</p>
		
		<p>
		A Protein graph is defined as labeled, undirected graph. The vertices correspond to the SSEs or ligands, and they are labeled with
		the SSE type (alpha helix, beta strand or ligand). The vertices of the Protein graph are enumerated as they occur in the sequence from the N- to the C-terminus.
		
		
		<!--
		According to the type of atoms forming the contact, there are backbone-backbone-contacts, sidechain-sidechain-contacts, and sidechain-backbone
		contacts. Two vertices are connected, if there are at least two backbone-backbone-contacts or two sidechain-backbone-contacts or three
		sidechain-sidechain contacts.-->
		</p>
		
		<p>
		The edges of the Protein graph represent spatial adjacencies of SSEs. These adjacencies are defined through atom contacts between SSEs, based on the van-der-Waals radius.
		According to this direction two spatial neighboured SSEs, which are connected, could have a parallel (p), anti-parallel (a), or mixed (m)
		neighbourhood. 
		</p>
		
		<p class="imgCenter"><img src="./images/protein_graph.png" alt="Protein graph" title="Protein graph" class="img-responsive imgFormAboutphp2"/></p>
		
		
		<br/>
		<a class="anchor" id="contacts"></a>
		<h4> Atom contacts and SSE contacts </h4>
		<p>
		Protein graphs are based on contacts between SSEs. Here, we explain how an SSE contact is defined in the PTGL. The computation of SSE contacts is a 2-step process: first, the atom contacts for the residues are computed, and the residues are assigned to SSEs. Then, a ruleset is used to determine whether enough atom contacts exist between a pair of SSEs to define this as a contact on the SSE level.		
		</p>

		
		<b>Atom level contacts</b><br/>
		We use a hard-sphere model to compute atom contacts. Atom positions are parsed from PDB files, and a collision sphere with radius 2 Angstroem is assigned to each protein atom (hydrogens are ignored). For ligand atoms, the radius is 3 Angstroem.
		If the collision spheres of 2 atoms from different residues overlap, this is considered an atom level contact.
		<br/>
		The different atoms of a residue are backbone atoms or side chain atoms. Based on this differentation, each atom level contact is assigned to one of the following types:
		
		<ul>
		  <li>BB: backbone - backbone contact</li>
		  <li>BC: backbone - side chain contact</li>
		  <li>CC: side chain - side chain contact</li>
		  <li>LB: ligand - backbone contact</li>
		  <li>LC: ligand - side chain contact</li>
		  <li>LL: ligand - ligand contact</li>
		  <li>LX: ligand - non-ligand contact, i.e., LX = LB or LC</li>
		</ul>
		
		</p>
		
		<b>SSE level contacts</b><br/>
		Based on the atom level contacts, a rule set is applied to decide whether or not a pair of SSEs is in contact. The rules depend on the SSE types, and are as follows:
		<table border="1px" padding="15px">
		<tr><th>SSE 1 type</th><th>SSE 2 type</th><th>Required contacts</th><tr>
		<tr><td>Beta strand</td><td>Beta strand</td><td>BB &gt; 1 or CC &gt; 2</td><tr>
		<tr><td>Helix</td><td>Beta strand</td><td>(BB &gt; 1 and BC &gt; 3) or CC &gt; 3</td><tr>
		<tr><td>Helix</td><td>Helix</td><td>BC &gt; 3 or CC &gt; 3</td><tr>
		<tr><td>Ligand</td><td><i>Any type</i></td><td>LX &gt;= 1</td><tr>
		</table>
		</p>
		
		<p>		
		For more details on the contact definition, please see the following publication: <b>Schäfer T, May P, Koch I (2012). <i>Computation and Visualization of Protein Topology Graphs Including Ligand Information.</i> German Conference on Bioinformatics 2012; 108-118</b>.
		</p>
		
		
		
		<br/>
		<a class="anchor" id="graphTypes"></a>
		<h4> Graph Types </h4>
		
		<p>		
		If only a certain SSE type is of interest, the graph modelling allows to exclude the non-interesting SSE types.
		According to the SSE type of interest, the Protein graph can be defined as an <a href="#alphaGraph">Alpha graph</a>, <a href="#betaGraph">Beta graph</a>, or <a href="#alphaBetaGraph">Alpha-Beta graph</a>.
		If you are interested in the ligands as well, you can also use the <a href="#alphaLigGraph">Alpha-Ligand graph</a>, the <a href="#betaLigGraph">Beta-Ligand graph</a>,
		and the <a href="#alphaBetaLigGraph">Alpha-Beta-Ligand graph</a>.</p>
		
		<p>
		The Alpha graph only contains alpha helices and the contacts between them. The Alpha-Beta graph contains alpha helices, beta strands and the contacts betweem them. And so on.
		</p>
		
		<a class="anchor" id="explainImages"></a>
		<h3>Interpreting the graph images</h3>

		<p>
		In the graph visualizations available on the PTGL server, the SSEs
		are ordered as red circles (helices), black quadrats (strands), or magenta rings (ligands) on a straight line according to their sequential order from the N- to
		the C-terminus. The spatial neighbourhoods are drawn as arcs between SSEs. The edges are coloured according to their labelling, red for
		parallel, green for mixed, blue for anti-parallel, and magenta for ligand neighbourhood. Here is the key for the images:
		</p>
		
		<p class="imgCenter"><img src="./images/vplg_legend.png" alt="PTGL graph image key" title="PTGL graph image key" class="img-responsive imgFormAboutphp2"/></p>
		
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>

		<a class="anchor" id="exampleGraphTypes"></a>
		<h4>PDB 7TIM as an example for the different graph types</h4>
		
		<br>
		<br>
		<a class="anchor" id="alphaGraph"></a>
		<h5> <u>Alpha Graph</u> </h5>
		The Alpha-Graph of the protein 7TIM chain A consisting only of 13 helices.
		<p class="imgCenter"><img src="./images/7tim_A_alpha_PG.png" alt="Alpha Graph of 7timA" title="Alpha Graph of 7timA" class="img-responsive imgFormAboutphp2"/></p>		


		<br>
		<br>
		<a class="anchor" id="betaGraph"></a>
		<h5> <u>Beta Graph</u> </h5>
		The Beta-Graph of the protein 7TIM chain A consisting only of 8 strands.  Note the beta barrel in the protein, which is clearly visible as a circle of parallel beta-strands in this graph.
		<p class="imgCenter"><img src="./images/7tim_A_beta_PG.png" alt="Alpha Graph of 7timA" title="Alpha Graph of 7timA" class="img-responsive imgFormAboutphp2"/></p>

		<br>
		<br>
		<a class="anchor" id="alphaBetaGraph"></a>
		<h5> <u>Alpha-Beta Graph</u> </h5>
		The Alpha-Beta Graph of the protein 7TIM chain A consisting of 21 SSEs (13 helices and 8 strands). 
		<p class="imgCenter"><img class="img-responsive imgFormAboutphp2" src="./images/7tim_A_albe_PG.png" alt="Alpha-Beta Graph of 7timA" title="Alpha-Beta Graph of 7timA"></p>
		
		<br>
		<br>
		<a class="anchor" id="alphaLigGraph"></a>
		<h5> <u>Alpha-Ligand Graph</u> </h5>
		The Alpha-Ligand Graph of the protein 7TIM chain A consisting of 13 helices and 1 ligand.
		<p class="imgCenter"><img src="./images/7tim_A_alphalig_PG.png" alt="Alpha-Ligand Graph of 7timA" title="Alpha-Ligand Graph of 7timA" class="img-responsive imgFormAboutphp2"/></p>


		<br>
		<br>
		<a class="anchor" id="betaLigGraph"></a>
		<h5> <u>Beta-Ligand Graph</u> </h5>
		The Beta-Ligand-Graph of the protein 7TIM chain A consisting of 8 strands and 1 ligand.
		<p class="imgCenter"><img src="./images/7tim_A_betalig_PG.png" alt="Beta-Ligand Graph of 7timA" title="Beta Graph of 7timA" class="img-responsive imgFormAboutphp2"/></p>

		<br>
		<br>
		<a class="anchor" id="alphaBetaLigGraph"></a>
		<h5> <u>Alpha-Beta-Ligand Graph</u> </h5>
		The Alpha-Beta-Ligand Graph of the protein 7TIM chain A consisting of 22 SSEs (13 helices, 8 strands and 1 ligand). 
		<p class="imgCenter"><img class="img-responsive imgFormAboutphp2" src="./images/7tim_A_albelig_PG.png" alt="Alpha-Beta-Ligand Graph of 7timA" title="Alpha-Beta Graph of 7timA"></p>
		
		
		
		
		

		
		
		
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		
		
		<br>
		<br>
		<a class="anchor" id="foldingGraph"></a>
		<h4>Folding Graphs</h4>
		<p>A connected component of the <a href="#proteinGraph">Protein graph</a> is called Folding graph. Folding graphs are denoted with capital letters in alphabetical order
		according to their occurrence in the sequence, beginning at the N-terminus.</p>

		<p><a href="#proteinGraph">Protein graphs</a> are built of one or more Folding graphs. Below, you find the <a href="#rasmolbec">schematic representation</a> of the antigen receptor protein 1BEC. Helices are coloured red and strands blue. 1BEC is a transport membrane protein
		that detects foreign molecules at the cell surface. It has two domains, which are represented by the Folding graphs A and E, which are mainly
		built by strands. The protein consists of one chain A and exhibits six Folding graphs. Two large Folding graphs (Folding graphs 1BEC_A and
		1BEC_E), and four Folding graphs 1BEC_B, 1BEC_C, 1BEC_D, and 1BEC_F consisting only of a single helix (see <href="#alphaBeta1bec">Protein graph of 1bec</a>: helices 9,
		11, 14, and 22). Folding graphs consisting of only one SSE are found mostly at the protein surface and not in the protein core.</p>
		Especially in beta-sheet containing Folding graphs, the maximal vertex degree of the Folding graphs is rarely larger than two. Thus, we distinguish
		between so-called bifurcated and non-bifurcated topological structures. A <a href="#proteinGraph">Protein graph</a> or a Folding graph is called bifucated, if there is any
		vertex degree greater than 2, if not, the graph is non- bifurcated. 
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		<br>
		<br>
		<a class="anchor" id="rasmolbec"></a>
		<h5> 3D structure of 1BEC: </h5>
			<p><img src="./images/1bec.gif" alt="3D structure of 1BEC" title="3D structure of 1BEC" class="img-responsive imgFormAboutphp2"/></p>


		<a class="anchor" id="alphaBeta1bec"></a>
		<h5> Alpha-Beta Protein graph of 1BEC: </h5>
			<p><img src="./images/1becA_albe.0.png" alt="Alpha-Beta Protein graph of 1BEC" title="Alpha-Beta Protein graph of 1BEC" class="img-responsive imgFormAboutphp2"/></p>
		<h5> Alpha-Beta Folding graph A of 1BEC: </h5>
			<p><img src="./images/1becAAa_al.0.png" alt="Alpha-Beta Folding graph A of 1BEC" title="Alpha-Beta Folding graph A of 1BEC" class="img-responsive imgFormAboutphp2"/></p>
		<h5> Alpha-Beta Folding graph B of 1BEC: </h5>
			<p><img src="./images/1becAEa_al.0.png" alt="Alpha-Beta Folding graph B of 1BEC" title="Alpha-Beta Folding graph B of 1BEC" class="img-responsive imgFormAboutphp2"/></p>
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		<br>
		<br>
		<a class="anchor" id="linearNot"></a>
		<h4>Linear Notations </h4>
		<p>A notation serves as a unique, canonical, and linear description and classification of structures. The notations for Folding graphs reveal to
		the feature of protein structure as a linear sequence of amino acids, and describe the arrangement of SSEs correctly and completely.</p>
		<p>There are two possibilities of representing Protein graphs: first, one can order the SSEs in one line according to their occurrence in sequence,
		or second, according to their occurrence in space. In the first case, the <a href="#adj">adjacent</a> notation, ADJ, the <a href="#red">reduced</a> notation, RED, and the <a href="#seq">sequence</a>
		notation, SEQ, SSEs are ordered as points on a straight line according to their sequential order from the N- to the C-terminus.</p>
		<p>It is difficult to draw the spatial arrangements of the SSEs in a straight line, because in most proteins SSEs exhibit more than two spatial
		neighbours. Therefore, the second description type, the <a href="#key">key</a> notation, KEY, can be drawn only for non-bifurcated Folding graphs. Helices and
		strands are represented by cylinders and arrows, respectively. The sequential neighbourhood is described by arcs between arrows and cylinders.</p>
		The notations are written in different brackets: [] denote non-bifurcated, {} bifurcated folding graphs, and () indicate barrel structures. 
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		<br>
		<br>
		<a class="anchor" id="adj"></a>
		<a class="anchor" id="red"></a>
		<h5> <u>The adjucent and reduced notation</u> </h5>
		<p>All vertices of the <a href="#proteinGraph">Protein graph</a> are considered in the adjacent (ADJ) notation of a Folding graph. SSEs of the Folding graph are ordered
		according to their occurrence in the sequence. Beginning with the first SSE and following the spatial neighbourhoods the sequential distances
		are noted followed by the neighbourhood type.</p>
		The reduced (RED) notation is the same as for ADJ notation, but only those SSEs of the considered Folding graph count. See below, the ADJ and
		RED notations of the Beta-Folding graph E in human alpha thrombin chain B(1D3T). The beta sheet consists of six strands arranged both in
		parallel with one additional mixed edge to helix 12.<br><br>

		<h5> ADJ Notation </h5>
			<p><img src="./images/1d3tBEa_albe.png" alt="Adjacent notation" title="Adjacent notation" class="img-responsive imgFormAboutphp2"/></p>
		<h5> RED Notation </h5>
			<p><img src="./images/1d3tBEr_albe.png" alt="Reduced notation" title="Reduced notation" class="img-responsive imgFormAboutphp2"/></p>
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>

		<br>
		<br>
		<a class="anchor" id="key"></a>
		<h5> KEY Notation </h5>
		The KEY notation is very close to the topology diagrams of biologists, e.g. Brändén and Tooze (1999). Topologies are described by diagrams of
		arrows for strands and cylinders for helices. As in the <a href="#red">RED</a> notation SSEs of the considered Folding graph are taken into account. SSEs are
		ordered spatially and are connected in sequential order. Beginning with the first SSE in the sequence and following the sequential edges,
		the spatial distances are noted; in <a href="#alphaBetaGraph">Alpha-Beta</a> graphs followed by the type of the SSE, h for a helix and e for a strand. If the arrangement
		of SSEs is parallel an x is noted (Richardson(1977)). In this case the protein chain moves on the other side of the sheet by crossing the
		sheet (cross over). Antiparallel arrangements are called same end, and are more stable, Chothia and Finkelstein (1990). Mixed arrangements
		are defined as same end. The notation starts with the type of the first SSE. See the KEY notation of the <a href="#alphaBetaGraph">Alpha-Beta</a> <a href="#foldingGraph">Folding graph</a> B chain
		B of the histocompatibility antigen (1IEB). The Folding graph consists of 3 helices and 4 strands. This topology exhibits one cross over
		connection from helix 6 to helix 7 and forms an <a href="#alphaBetaGraph">Alpha-Beta</a> barrel structure. 

		<p><img src="./images/1iebBk.png" alt="Key notation" title="Key notation" class="img-responsive imgFormAboutphp2"/></p>
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		<br>
		<br>
		<a class="anchor" id="seq"></a>
		<h5> SEQ Notation </h5>
		This notation is the same as the <a href="#adj">ADJ</a> notation, but the sequential differences are counted. Although the SEQ notation is trivial, the notation
		can be useful, for example, searching for ψ-loops requires a special SEQ notation. 

		<p><img src="./images/1ars_Bs_beta.png" alt="Sequence notation" title="Sequence notation" class="img-responsive imgFormAboutphp2"/></p>
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		<br>
		<br>
		
		The linear notations enable you to search the <?php echo "$SITE_TITLE"; ?> for protein motifs (and arbitruary other 3D arrangements of SSEs). When you search for a motif, SQL-based string matching in the linear notation strings is used to find all folding graphs which match a query.
		
                <br>
		<br>
		
		
		
		
		
		<a class="anchor" id="linking"></a>
		<h3>Linking <?php echo "$SITE_TITLE"; ?></h3>
		<p>You can link <?php echo "$SITE_TITLE"; ?> in several ways, depending on the kind of data you want:</p>
		
		<ul>
		    <li>Link to all protein graphs of a chain:</li>
		    <ul>
		        <li>Format: <?php echo "$SITE_BASE_URL"; ?>/results.php?q=&lt;pdbid&gt;&lt;chain&gt;</li>
		        <li>The allowed values for the parameters are:</li>
		        <ul>
		        <li>&lt;pdbid&gt;: a PDB identifier</li>
		        <li>&lt;chain&gt;: a PDB chain name</li>
		        </ul>
		        <li>Example for PDB 7tim, chain A: <?php echo "$SITE_BASE_URL"; ?>/results.php?q=7timA</li>
		    </ul>
		    <li>Link to all folding graph linear notations of a protein graph:</li>
		    <ul>
		        <li>Format: <?php echo "$SITE_BASE_URL"; ?>/foldinggraphs.php?pdbchain=&lt;pdbid&gt;&lt;chain&gt;&amp;graphtype_int=&lt;graphtype_code&gt;&amp;notationtype=&lt;notation&gt;</li>
		        <li>The allowed values for the parameters are:</li>
		        <ul>
		        <li>&lt;pdbid&gt;: a PDB identifier</li>
		        <li>&lt;chain&gt;: a PDB chain name</li>
		        <li>&lt;graphtype_code&gt;: 1=alpha, 2=beta, 3=albe, 4=alphalig, 5=betalig, 6=albelig</li>
		        <li>&lt;notation&gt;: a notaion: adj, red, seq or key</li>
		        </ul>
		        <li>Example for the ADJ notation folding graphs of the alpha protein graph of PDB 7tim chain A: <?php echo "$SITE_BASE_URL"; ?>foldinggraphs.php?pdbchain=7timA&amp;graphtype_int=1&amp;notationtype=adj</li>
		    </ul>
		    
		</ul>
		
		
		
		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		
		
		<a class="anchor" id="api"></a>
		<h3>The <?php echo "$SITE_TITLE"; ?> REST API</h3>
		<p>We are offering a REST API for programmers. Please see the <a href="./api/" target="_blank">API documentation</a> for details.</p>
		


		<div class="topLink"><a href="#" class="topLink"><i class="fa fa-2x fa-long-arrow-up"></i></a></div>
		
		<br><br><br>

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
