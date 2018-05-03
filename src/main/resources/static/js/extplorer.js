const ROOT_FOLDER = "ROOT";
const FILE_SIZES = ["KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB"];
var currentDir = '/';
var currentDirPage = 1;

$( document ).ready(function() {
	$("#checkAll").click(function() {
		$('input:checkbox').not(this).prop('checked', this.checked);
		toggleDelete();
	});
	
	$("#btnSubmit").click(function (event) {
        event.preventDefault();
        uploadFiles();
    });
	
	$("#btnDelete").click(function (event) {
        if (confirm('Do you really want to delete selected files?')) {
        	deleteFiles();
        }
    });
    
    document.querySelector('#selectedFiles').addEventListener('change', uploadFiles, false);

    $("#btnUpload").click(function(event) {
    	event.preventDefault();
    	$("#selectedFiles").trigger('click');
    });
    
    
    
    var win = $(window);
	// Each time the user scrolls
    win.scroll(function() {
		// End of the document reached?
		if ($(document).height() - win.height() == win.scrollTop()) {
			loadFiles(currentDir, ++currentDirPage);
		}
	});
    
    
	loadFiles(currentDir);
});

function isLoading() {
	return $("#loading:visible").length > 0;
}

function alertMessage(msg, isWarn) {
	const msgDelay = 5000;
	if (isWarn) {
		$('#alert').removeClass().addClass('alert alert-warning').html(`
			<strong>Failed!</strong> ${msg}
		`).show().delay(msgDelay).fadeOut('slow');
	} else {
		$('#alert').removeClass().addClass('alert alert-success').html(`
			<strong>Success!</strong> ${msg}
		`).show().delay(msgDelay).fadeOut('slow');
	}
}

function uploadFiles(event) {
	const filesCount = event.target.files.length;
	
	console.log("uploading %d files ...", filesCount);
	
	 // Get form
    var form = $('#fileUploadForm')[0];
    var data = new FormData(form);

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/api/uploadfile",
        data: data,
        processData: false, //prevent jQuery from automatically transforming the data into a query string
        contentType: false,
        cache: false,
        beforeSend: function() {
        	$("#btnUpload").attr('disabled', 'disabled');
        	$("#btnUpload").text(`Uploading ${filesCount} file${filesCount > 1 ? 's' : ''}...`);
        },
        success: function (data) {
            alertMessage('File(s) were uploaded');
        },
		error: function(xhr, status, error) {
			var err = (xhr.responseText) ? JSON.parse(xhr.responseText) : null;
			var msg = (err) ? err.message : 'server side error';
			alertMessage('Error occured while uploading files: ' + msg, true);
		},
		complete: function() {
            $("#btnUpload").removeAttr('disabled');
            $("#btnUpload").text('Upload');
            loadFiles(currentDir);
        }
    });
}

function convertSize(bytes) {
	var result = bytes + " bytes";
    // optional code for multiples approximation
    for (var aMultiples = FILE_SIZES, nMultiple = 0, nApprox = bytes / 1024; nApprox > 1; nApprox /= 1024, nMultiple++) {
        result = nApprox.toFixed(3) + " " + aMultiples[nMultiple];
    }
    
    return result;
}

function deleteFiles() {
	var filesToDelete = [];
	
	var checkedFiles = $("input:checked:not(#checkAll)").closest('tr').find('a');
	
	$.each(checkedFiles, function(i, val) {
		if (!currentDir.endsWith('/')) {
			filesToDelete.push(currentDir + '/' + $.trim(val.text));
		} else {
			filesToDelete.push(currentDir + $.trim(val.text));
		}
	});
	
	console.log(filesToDelete);
	
	$.ajax({
		type : "DELETE",
		url :  `/api/delete/`,
		data: JSON.stringify(filesToDelete),
		dataType: 'json',
		contentType: "application/json; charset=utf-8",
		beforeSend: function() {
			const filesCount = $("input:checked:not(#checkAll)").length;
			$('#btnDelete').attr('disabled', 'true');
			$('#btnDelete').text(`Deleting ${filesCount} file${filesCount > 1 ? "s" : ""}...`);
		},
		success: function() {
			alertMessage('File(s) were deleted');
			
		},
		error: function(xhr, status, error) {
			var err = (xhr.responseText) ? JSON.parse(xhr.responseText) : null;
			var msg = (err) ? err.message : 'server side error';

			alertMessage('Error occured while deleting files: ' + msg, true);
		},
		complete: function() {
			$('#btnDelete').text('Delete');
			loadFiles(currentDir);
		}
	});
}

function loadFiles(path, page) {
	
	if (!isLoading()) {
		$('#loading').show();	
	}

	currentDir = path;
	$("input:hidden[name=uploadDir]").first().val(currentDir);
	
	
	$.ajax({
		type : "GET",
		url :  `/api/files/`,
		data: {
			page: page,
			relativePath: path
		},
		beforeSend: function() {
			if (!page) {
				$("#filesTable tbody").empty();
				currentDirPage = 1;
			}
		},
		success: function(jsonList){
			updateBreadcrumb(path);
			
			//return page counter to previous value
			if (!jsonList || jsonList.length < 1) {
				if (currentDirPage > 0) {
					currentDirPage--;
				}
			}
			
			$.each(jsonList, function(i, file) {
				var filePath = '/api/file' + currentDir + (currentDir.endsWith('/') ? '' : '/') + file.name;
				var relativePath = path.endsWith('/') ? (path + file.name) : (path + '/' + file.name);
				
				$("#filesTable tbody").append(`
				    <tr>
						<td>
							<input type="checkbox" onClick='checkboxClicked(this)'></input>
						</td>
						<td>
							<a  ${ file.isFile ? ` href="${filePath}" ` : ` href="#" class="font-weight-bold" onClick="loadFiles('${relativePath}')" `} >
								${file.name}
							</a>
						</td>
						<td>${convertSize(file.fileSize)}</td>
						<td>${file.isFile ? 'FILE' : 'FOLDER'}</td>
						<td>${file.lastModified}</td>
					</tr>
				`);
			});
		},
		error : function(e) {
			$("#result").html(e.responseText);
		}, 
		complete: function() {
			$('#loading').hide();	
		}
	});	
}

function toggleDelete() {
	var enable = $("input:checked:not(#checkAll)").length > 0;
	
	if (enable) {
		$('#btnDelete').removeAttr('disabled');
	} else {
		$('#btnDelete').attr('disabled', 'true');
	}
}

function checkboxClicked(element) {
	if (!element.checked) {
		$('#checkAll').prop('checked', false); 
	} else {
		var checkAll = $("input:checkbox:not(:checked):not(#checkAll)").length === 0;
		
		if (checkAll) {
			$('#checkAll').prop('checked', true);
		}
	}
	
	toggleDelete();
}

function updateBreadcrumb(path) {
	$('#checkAll').prop("checked", false);
	$("#breadcrumb").empty();
	
	if (!path || path === '/') {
		$("#breadcrumb").append(`<li class="breadcrumb-item active" aria-current="page">${ROOT_FOLDER}</li>`);
	} else {
		var pathItems = path.split(/[\/]/);
		
		$.each(pathItems, function(i, item) {
			var isLast = i === pathItems.length - 1;
			var isFirst = i === 0;
			
			if (isLast) {
				$("#breadcrumb").append(`<li class="breadcrumb-item active" aria-current="page">${item}</li>`);
			} else {
				if (isFirst) {
					$("#breadcrumb").append(`<li class="breadcrumb-item"><a href="#" onClick="loadFiles('/')">${ROOT_FOLDER}</a></li>`);
				} else {
					var itemPath = pathItems.slice(0, i+1).join('/');
					
					$("#breadcrumb").append(`<li class="breadcrumb-item"><a href="#" onClick="loadFiles('${itemPath}')">${item}</a></li>`);
				}
			}
		});
	}
}
