<?
	$status;
	exec('jps', $status);
	$timestamp;
	exec('cat /home/ubuntu/shell/timestamp.out', $timestamp);
        $isLaunching;
	exec('cat /var/www/html/shell_script/hadoop_launching.out', $isLaunching);
?>

<!DOCTYPE html>

<html lang="en">

  <head>

    <meta charset="utf-8">

    <title>Bootstrap, from Twitter</title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <meta name="description" content="">

    <meta name="author" content="">



    <!-- Le styles -->

    <link href="css/bootstrap.css" rel="stylesheet">

    <style>

      body {

        padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */

      }
      dt { padding-top:30px; }
      dd { padding-top:30px; }

    </style>
    <script>
	function startSystem(){
		window.open("/popup.php", 'popup', 'scrollbar=no,width=500,height=300,resizable=no,status=no,toolbar=no,menubar=no');
	}
    </script>
    <link href="css/bootstrap-responsive.css" rel="stylesheet">



    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->

    <!--[if lt IE 9]>

      <script src="js/html5shiv.js"></script>

    <![endif]-->



    <!-- Fav and touch icons -->

    <link rel="apple-touch-icon-precomposed" sizes="144x144" href="ico/apple-touch-icon-144-precomposed.png">

    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="ico/apple-touch-icon-114-precomposed.png">

      <link rel="apple-touch-icon-precomposed" sizes="72x72" href="ico/apple-touch-icon-72-precomposed.png">

                    <link rel="apple-touch-icon-precomposed" href="ico/apple-touch-icon-57-precomposed.png">

                                   <link rel="shortcut icon" href="ico/favicon.png">

  </head>



  <body>



    <div class="navbar navbar-inverse navbar-fixed-top">

      <div class="navbar-inner">

        <div class="container">

          <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">

            <span class="icon-bar"></span>

            <span class="icon-bar"></span>

            <span class="icon-bar"></span>

          </button>

          <a class="brand" href="index.html">Number Plate</a>

          <div class="nav-collapse collapse">

            <ul class="nav">

              <li class="active"><a href="index.php">System Status</a></li>

              <li><a href="file.php">File in System</a></li>

              <li><a href="analysis.php">Analysis</a></li>

            </ul>

          </div><!--/.nav-collapse -->

        </div>

      </div>

    </div>



    <div class="container">
		<dl class="dl-horizontal">
			<dt>Hadoop Status</dt>
			<dd><? if(count($status) > 3) {
				echo "Launching";
				} else {
				echo "Terminated";
			} ?></dd>
			<dt>System Launch</dt>
		
			<? if ($isLaunching[0] == 1 ) { ?>
			<dd><a href="#" class="btn btn-danger">System Busy</a></dd>
			<? } else { ?>
			<!--<dd><a href="run_hadoop_application.php" class="btn btn-primary">Lauch</a></dd>-->
			<dd><a href="javascript:startSystem();" class="btn btn-primary">Lauch</a></dd>
			<? } ?>
			<dt>Recently Work</dt>
			<dd><?= $timestamp[0] ?></dd>
		</dl>

    </div> <!-- /container -->



    <!-- Le javascript

    ================================================== -->

    <!-- Placed at the end of the document so the pages load faster -->

    <script src="js/jquery.js"></script>

    <script src="js/bootstrap-transition.js"></script>

    <script src="js/bootstrap-alert.js"></script>

    <script src="js/bootstrap-modal.js"></script>

    <script src="js/bootstrap-dropdown.js"></script>

    <script src="js/bootstrap-scrollspy.js"></script>

    <script src="js/bootstrap-tab.js"></script>

    <script src="js/bootstrap-tooltip.js"></script>

    <script src="js/bootstrap-popover.js"></script>

    <script src="js/bootstrap-button.js"></script>

    <script src="js/bootstrap-collapse.js"></script>

    <script src="js/bootstrap-carousel.js"></script>

    <script src="js/bootstrap-typeahead.js"></script>



  </body>

</html>
