$(document).ready(
		function() {

			$(document).bind("contextmenu", function(e) {
				return false;
			});
//			treeview implmentation
			var treeViewdata;
			$.ajax({
				url: "loadProfile",
				type: 'POST',
				async: false,
				dataType: "json",
				success: function (result) {
					treeViewdata = result;
				},
				error: function (e) {
					console.log(e);
					alert("error"+e);
				}
			});

			console.log("After ajax call, treeview is ", treeViewdata);
			var profilearr = treeViewdata.UserProfile[0].Profile;
			console.log(profilearr);
			//var profiledata = $.parseJSON(profilearr[0]);


			var itemsArray = new Array();
			var testObject = new Object();
			testObject.id=treeViewdata.UserProfile[0].userid;
			testObject.items = treeViewdata.UserProfile[0].Profile;
			itemsArray.push(testObject);

			var inlineDefault = new kendo.data.HierarchicalDataSource({
				data : itemsArray,
				schema : {
					model : {
						id : "id",
						children : "items"
					}
				}
			});





			var tree = $("#treeview-left").kendoTreeView({
				dataSource : inlineDefault,
				dataTextField : "id",
				select : function(event) {
					//console.log("here");
					var parent=null,parentparent=null;
					var child = tree.dataItem(event.node);
					console.log("child",child);

					if(!(child.parentNode() == undefined)){
						parent = child.parentNode();
						parentparent = parent.parentNode();
					}
					//console.log("child", child);
					//console.log("parent", parent);
					//console.log("parentparent", parentparent);
					//alert("check");
					//alert("node: " + JSON.stringify(child)+ "\nparent: "+ JSON.stringify(parent)+ "\nparentparent: "+ JSON.stringify(parentparent) /* + " parent:" +JSON.stringify(tree.dataItem(event.node)).parent()*/);
				}
			}).data("kendoTreeView");
//			treeview implementation 

			var temp = treeViewdata.UserProfile[0].Profile;
			var workbenchData = "[";
			console.log("treeview profile", temp.length);
			console.log("here");
			console.log("temp is here ", temp);
			for(var i =0; i<temp.length;i++){
				if(i<temp.length-1){
					workbenchData += "{ \"text\" : "+"\""+temp[i].id+"\""+", \"value\" : "+"\""+temp[i].workbenchid+"\""+"},";
				}else{
					workbenchData += "{ \"text\" : "+"\""+temp[i].id+"\""+", \"value\" : "+"\""+temp[i].workbenchid+"\""+"}";
				}
			}
			workbenchData += "]"
			var projectData="[";
			var projectMap = new Object();
			var count = 1;
			for(var i = 0 ; i<temp.length ; i++){
				var tempProjectdata  = temp[i].items;
				for( var j = 0 ; j < tempProjectdata.length ; j++){
					console.log(" temp project data ", tempProjectdata);
					projectMap[tempProjectdata[j].id]=tempProjectdata[j].ProjectID;
					count++;
					/*projectData += "{ \"text\" : "+"\""+tempProjectdata[j].id+"\""+", \"value\" : "+"\""+count+"\""+"},";
					}else{
						projectData += "{ \"text\" : "+"\""+tempProjectdata[j].id+"\""+", \"value\" : "+"\""+count+"\""+"}]";
					}	*/

				}
			}

			var projectMapSize = 0;

			for (var i in projectMap){
				if (projectMap.hasOwnProperty(i)) {
					console.log(i, projectMap[i]);
					projectMapSize++;
				}
			}
			var m=0;
			for(var i =0 in projectMap){

				if(m<projectMapSize-1){
					projectData += "{ \"text\" : "+"\""+i+"\""+", \"value\" : "+"\""+projectMap[i]+"\""+"},";
				}else{
					projectData += "{ \"text\" : "+"\""+i+"\""+", \"value\" : "+"\""+projectMap[i]+"\""+"}";
				}
				m++;
			}
			projectData +="]"; 
			console.log("project data ", projectData);
			
			var serverMap = new Object();
			var serverData="[";
			var count = 1;
			for(var i = 0 ; i<temp.length ; i++){
				var tempProjectdata  = temp[i].items;
				for( var j = 0 ; j < tempProjectdata.length ; j++){
					var tempServerdata = tempProjectdata[j].items;
					for( var k = 0 ; k < tempServerdata.length ; k++){
						if(serverMap[tempServerdata[k].id] == undefined){
							console.log("server temp is here " , tempServerdata);
							serverMap[tempServerdata[k].id] = tempServerdata[k].ServerID;
							count++;
						}
					}
				}
			}

			var size = 0;

			for (var i in serverMap){
				if (serverMap.hasOwnProperty(i)) {
					console.log(i, serverMap[i]);
					size++;
				}
			}
			var j=0;
			for(var i =0 in serverMap){

				if(j<size-1){
					serverData += "{ \"name\" : "+"\""+ i +"\""+", \"id\" : "+"\""+ serverMap[i]+"\""+", \"checked\" : "+"\""+"false"+"\""+ "},";
				}else{
					serverData += "{ \"name\" : "+"\""+ i +"\""+", \"id\" : "+"\""+ serverMap[i]+"\""+", \"checked\" : "+"\""+"true"+"\""+ "}";
				}
				j++;
			}
			serverData+="]";
			console.log("server data ", serverData);


			function projectvalidateFields() {
				//var x = document.forms["myForm"]["fname"].value;
				var projectName = $("#projectname").val();
				if (projectName == null || projectName == "") {
					alert("Please fill all mandatory fields.");
					return false;
				}
				if (projectName > 30) {
					alert("Some of the fields are too long");
					return false;
				}

				return true;
			}
			$("#createprojectbutton").click(function() {
				if(projectvalidateFields()){

					var workbencId = $("#selectworkbench").val();
					var projectName = $("#projectname").val();
					var projectDesc = $("#projectdescription").val();

					var jsonString = {
							projectName: projectName,
							projectDesc: projectDesc,
							workbenchId: workbencId
					};

					console.log(JSON.stringify(jsonString));
					$.ajax({
						url: "project",
						type: 'POST',
						dataType: 'json',
						headers: { 
							'Content-Type': 'application/json' 
						},
						data: JSON.stringify(jsonString),
						beforeSend: function() {
							console.log("Before", JSON.stringify(jsonString));  
						},
						success: function (result) {
							alert("Project has been created successfully");
							console.log("data", result);
						},
						error: function () {
							alert("error");
						}
					});

				}
			});

			function workbenchvalidateFields() {
				//var x = document.forms["myForm"]["fname"].value;
				var workbenchName = $("#workbenchname").val();
				if (workbenchName == null || workbenchName == "") {
					alert("Please fill all mandatory fields.");
					return false;
				}
				if (workbenchName > 30) {
					alert("Some of the fields are too long");
					return false;
				}

				return true;
			}

			$("#createworkbenchbutton").click(function() {

				if(workbenchvalidateFields()){
					//var workbencId = $("#selectworkbench").val();
					var workbenchName = $("#workbenchname").val();
					var workbenchDesc = $("#workbenchdescription").val();

					var jsonString = {
							workbenchName: workbenchName,
							workbenchDesc: workbenchDesc
							//workbenchId: workbencId
					};

					console.log(JSON.stringify(jsonString));
					$.ajax({
						url: "workbench",
						type: 'POST',
						dataType: 'json',
						headers: { 
							'Content-Type': 'application/json' 
						},
						data: JSON.stringify(jsonString),
						beforeSend: function() {
							console.log("Before", JSON.stringify(jsonString));  
						},
						success: function (result) {
							alert("Workbench has been created successfully.");
							console.log("data", result);
						},
						error: function () {
							alert("error");
						}
					});

				}

			});

			function servervalidateFields() {

				var serverName = $("#servername").val();
				var hostIP = $("#hostnameip").val();
				var username = $("#username").val();
				var password = $("#password").val();
				var serverType = $("#servertype").val();
				var logFilePath = $("#logfilepath").val();
				var restartCmd = $("#restartCommand").val();

				if (serverName == null || serverName == "" || hostIP == null || hostIP == "" || username == null || username == "" || password == null || password == "") {
					alert("Please fill all mandatory fields.");
					return false;
				}
				if (serverName > 30 || hostIP > 30 || username > 30 || password > 30 || serverType > 30 || logFilePath > 30 || restartCmd > 100) {
					alert("Some of the fields are too long");
					return false;
				}

				return true;
			}

			$("#createserverbutton").click(function() {
				if(servervalidateFields()) {
					var projectId = $("#selectproject").val();
					var serverName = $("#servername").val();
					var serverDesc = $("#serverdescription").val();
					var hostIP = $("#hostnameip").val();
					var username = $("#username").val();
					var password = $("#password").val();
					var serverType = $("#servertype").val();
					var logFilePath = $("#logfilepath").val();
					var restartCmd = $("#restartCommand").val();

					var jsonString = {
							serverName: serverName,
							serverDesc: serverDesc,
							hostIP: hostIP,
							username: username,
							password: password,
							serverType: serverType,
							logFilePath: logFilePath,
							restartCmd: restartCmd,
							projectId: projectId
					};

					console.log(JSON.stringify(jsonString));
					$.ajax({
						url: "server",
						type: 'POST',
						dataType: 'json',
						headers: { 
							'Content-Type': 'application/json' 
						},
						data: JSON.stringify(jsonString),
						beforeSend: function() {
							console.log("Before", JSON.stringify(jsonString));  
						},
						success: function (result) {
							alert("Server has been created successfully");
							console.log("data", result);
						},
						error: function () {
							alert("error");
						}
					});
				}

			});

			function filevalidateFields() {

				var filePath = $("#filepath").val();
				var fileName = $("#filename").val();
				if (filePath == null || filePath == "") {
					alert("Please fill all mandatory fields.");
					return false;
				}
				if (fileName > 30) {
					alert("Some of the fields are too long");
					return false;
				}

				return true;
			}

			$("#addfilebutton").click(function() {
				if(filevalidateFields()) {

					var fileName = $("#filename").val();
					var fileDesc = $("#filedescription").val();
					var filePath = $("#filepath").val();
					var serverId = [];
					$('#servercheckboxes input:checked').each(function() {
						serverId.push($(this).attr('id'));
					});

					var jsonString = {
							fileName: fileName,
							fileDesc: fileDesc,
							filePath: filePath,
							serverId: serverId
					};

					console.log(JSON.stringify(jsonString));
					$.ajax({
						url: "addfile",
						type: 'POST',
						dataType: 'json',
						headers: { 
							'Content-Type': 'application/json' 
						},
						data: JSON.stringify(jsonString),
						beforeSend: function() {
							console.log("Before", JSON.stringify(jsonString));  
						},
						success: function (result) {
							alert("File has been addd successfully");
							console.log("data", result);
						},
						error: function () {
							alert("error");
						}
					});
				}
			});


			var workbenches = JSON.parse(workbenchData);
			console.log("workbenches", workbenches);/*[ {
				text : "DEV",
				value : "1"
			}, {
				text : "Staging",
				value : "2"
			}, {
				text : "Production",
				value : "3"
			} ];*/

			var projects = JSON.parse(projectData);/*[ {
				text : "SOLR",
				value : "1"
			}, {
				text : "File Sharing",
				value : "2"
			}, {
				text : "CAS",
				value : "3"
			} ];*/

			var servers = JSON.parse(serverData);/*[ {
				name : "Server 1",
				id : "1",
				checked : "true"
			}, {
				name : "Server 2",
				id : "2",
				checked : "false"
			}, {
				name : "Server 3",
				id : "3",
				checked : "true"
			} ];*/
			

			$("#getfile").click(function() {
				/*var fileWindow = window.open("getfile", null, null, null);*/
				var fileID = $("#fileid").val();
				console.log("fileId", fileID);
				var fileWindow = window.open("http://localhost:8080/ConfigAsService/getfile?fileid="+fileID, null, null, null);
				fileWindow.servers = servers;
				console.log("Sending to showfile window", servers);
			});
			
			$("#tabstrip").kendoTabStrip({
				animation : {
					open : {
						effects : "fadeIn"
					}
				}
			});


			/*{
					"GetMenu" : [ {
						"OutletCode" : "BOL",
						"MenuGroup" : [

						               {
						            	   "ParentId" : 1,
						            	   "ItemName" : "BEER",
						            	   "Items" : [ {
						            		   "ItemId" : 239,
						            		   "ParentId" : 1,
						            		   "ItemName" : "HEINEKEN PINT BEER",
						            		   "Price" : "35.000"
						            	   }, {
						            		   "ItemId" : 241,
						            		   "ParentId" : 1,
						            		   "ItemName" : "HEINEKEN BOTLLE",
						            		   "Price" : "35.000"
						            	   } ]
						               }, {
						            	   "ParentId" : 2,
						            	   "ItemName" : "BREEZERS",
						            	   "Items" : [ {
						            		   "ItemId" : 110,
						            		   "ParentId" : 2,
						            		   "ItemName" : "BACARDI BREEZER",
						            		   "Price" : "35.000"
						            	   } ]
						               } ],
					} ]
			};*/



			$("#selectworkbench").kendoDropDownList({
				dataTextField : "text",
				dataValueField : "value",
				dataSource : workbenches,
				indexselectworkbench : 0,
				change : onChangeWorkbench
			});

			$("#selectproject").kendoDropDownList({
				dataTextField : "text",
				dataValueField : "value",
				dataSource : projects,
				index : 0,
				change : onChangeProject
			});

			function onChangeWorkbench() {
				var value = $("#selectworkbench").val();
			}
			;
			function onChangeProject() {
				var value = $("#selectproject").val();
			}
			;



			$.each(servers, function() {
				$("#servercheckboxes")
				.append(
						$("<p>").text(this.name).prepend(

								$("<input>").attr({'type':'checkbox','id':this.id})

								.val(this.id)));
			});
		});