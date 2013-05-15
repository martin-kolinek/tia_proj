require(["common"], (common) ->
    require(["jquery", "form_repeating", "partdef_select", "datepicker"], ($, fr, pdef) ->
        rep = fr.addRepeated("od_handle")
        pdef.register(rep, $("#pdef_modal"), $("#pdef_modal_content"), "partdef_input")
        pdef.initDesc("partdef_input")
        $(document).ready ->
            $("input[type=date]").datepicker({format:"yyyy-mm-dd"})
    )
)