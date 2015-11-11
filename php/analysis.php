<?
  $output;
  exec('bash /var/www/html/shell_script/show_result.sh', $output);
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

	  .dl-horizontal dt {

		width: auto;

	  }

	  .dl-horizontal dd {

		margin-left: 250px;

	  }

    </style>

    <link href="css/bootstrap-responsive.css" rel="stylesheet">



    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->

    <!--[if lt IE 9]>

      <script src="js/html5shiv.js"></script>

    <![endif]-->

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

          <a class="brand" href="#">Project name</a>

          <div class="nav-collapse collapse">

            <ul class="nav">

              <li><a href="index.php">System Status</a></li>

              <li><a href="file.php">File in System</a></li>

              <li class="active"><a href="analysis.php">Analysis</a></li>

            </ul>

          </div><!--/.nav-collapse -->

        </div>

      </div>

    </div>



    <div class="container">
		<table class="table table-condensed">

			<thead>

				<tr>

					<th>#</th>

					<th>Number</th>

					<th>Count</th>

				</tr>

			</thead>

			<tbody>
				<? $idx = 1; ?>
				<? for ($i = 0; $i < count($output); $i++) { ?>
				<?   $result = explode('	', $output[$i]); ?>
				<? if($result[0] != 1) { ?>
				<tr>
					<td><?= $idx++ ?></td>
					<td><?= $result[0] ?></td>
					<td><?= $result[1] ?></td>
				</tr>
				<? } ?>
				<? } ?>
			</tbody>

		</table>

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
