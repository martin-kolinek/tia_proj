require(["common"], (common) ->
    require(["partdef_select", "order_def_select"], (pdef, odef) ->
        pdef.initDesc("pdefhandle")
        odef.initDesc("odefhandle")
    )
)