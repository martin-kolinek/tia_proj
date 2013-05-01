require(["common"], (common) ->
    require(["jquery", "form_repeating", "router", "temp_files", "bootstrap", "dataTables", "partdef_select"], ($, repeat, router, tf, bs, dt, pdef) ->
        tf.create("cutplanfile")
        repHandle = repeat.addRepeated("pd_handle")
        pdef.register(repHandle, $("#part_def_modal"), $("#part_def_modal_content"), "pdef-id-hf", "pdef-desc")  
        pdef.initDesc("pdef-id-hf", "pdef-desc")
    )
)