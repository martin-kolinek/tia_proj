require(["common"], (common) ->
    require(["jquery", "semiproduct_select", "form_repeating", "router", "order_def_select", "id_desc"], ($, ss, rep, router, odef, iddesc) ->
    	
        iddesc.init("order-def", router.controllers.OrderController.orderDefDescription)
        iddesc.init("cphandle", router.controllers.CuttingPlans.cutPlanDescription)
        iddesc.init("sphandle", router.controllers.Semiproducts.getSemiproductDescription)
        
        repHandle = rep.addRepeated("part_handle")
        odef.register(repHandle, "order-def", $("#modal-order"), $("#modal-order-content"))
            
        $("#semiprod-button").click ->
            ss.selectSemiproduct($("#modal-sp"), $("#modal-sp-content"), $(".semiprod-id"), $(".semiproduct-desc"))
    )
)