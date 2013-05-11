require(["common"], (common) ->
    require(["jquery", "temp_files", "form_repeating", "partdef_select"], ($, tf, repeat, pdef) ->
        tf.create("cutplanfile")
        repHandle = repeat.addRepeated("pd_handle")
        pdefHandle = pdef.register(repHandle, $("#part_def_modal"), $("#part_def_modal_content"), "pdef_input")
        pdefHandle.added (ev) ->
        	alert(ev.partdef)
        pdef.initDesc("pdef_input")
    )
)