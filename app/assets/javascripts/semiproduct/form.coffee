require(["formRepeating"], (rep) ->
    rep.addRepeated("semiproducts", "addButton", "semiproduct", "semiprodTemplate", "removeSemiprod")
    typeMapping = 
    	"0":""
    	"1":"sheet_div"
    	"2":"circ_pipe_div"
    	"3":"square_pipe_div"
    fixDisplayed = ->
    	div_id = typeMapping[$(".type_select").val()]
    	for key, id of typeMapping
    		$("#"+id).hide()
    	$("#"+div_id).show()
    $(document).ready ->
    	$(".type_select").change ->
    		fixDisplayed()
    	fixDisplayed()
)
