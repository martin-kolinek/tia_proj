require(["common"], (common) ->
    require(["jquery", "form_repeating", "datepicker"], ($, rep) ->
        rep.addRepeated("sp_handle")
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
            $("input[type=date]").datepicker({format:"yyyy-mm-dd"})
    )
)
