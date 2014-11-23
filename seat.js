function setTableId(){
	var tds = document.getElementsByTagName("td")
	var i = 0
	for (td in tds){
		td.id = i
		i++
	}
}

var length = 0
for (columns in seats.1){
	length++
}

var row = 0, column = 0
var lastid = 0 
function changeState(id){
	var td = document.getElementById(id)
	if(td.class == 0 || td.class == 2 || ts.class == 4)
		return
	if(lastid != 0){
		ltd = document.getElementById(lastid)
		ltd.class = 1
	}
	td.class = 3
	lastid = id
	changeRecord(id)
}

function changeRecord(id){
	var tds = document.getElementsByTagName("td")
	l = 1
	for(td in tds){
		if(td.id == id)
			break
		l++
	}
	row = l / length
	column = l % length 
}