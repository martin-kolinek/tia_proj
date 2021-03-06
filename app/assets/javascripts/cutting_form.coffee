require(["common"], (common) ->
    require(["jquery", "semiproduct_select", "form_repeating", "router", "order_def_select", "id_desc"], ($, ss, rep, router, odef, iddesc) ->
    	
        iddesc.init("order-def", router.controllers.OrderController.orderDefDescription)
        iddesc.init("cphandle", router.controllers.CuttingPlans.cutPlanDescription)
        iddesc.init("sphandle", router.controllers.Semiproducts.getSemiproductDescription)
        
        repHandle = rep.addRepeated("part_handle")
        odef.register(repHandle, "order-def", $("#modal-order"), $("#modal-order-content"), $(iddesc.id("cphandle")).val())
            
        $("#semiprod-button").click ->
            filters = $(iddesc.id("order-def")).map ->
                $(this).data("filter")
            filter = filters.get().join()+","+$(iddesc.id("cphandle")).data("filter")+",hasfree"
            ss.selectSemiproduct($("#modal-sp"), $("#modal-sp-content"), $(iddesc.id("sphandle")), $(iddesc.desc("sphandle")), filter)
    )
)