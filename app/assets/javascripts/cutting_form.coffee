require(["common"], (common) ->
    require(["jquery", "semiproduct_select", "form_repeating", "router", "order_def_select"], ($, ss, rep, router, odef) ->
    	
        initCuttingPlans = ->
            cpdesc = $(".cutplan-desc")
            cpid = $(".cutplan-id").val()
            if(cpid)
                router.controllers.CuttingPlans.cutPlanDescription(cpid).ajax
                    success:(data, status, xhr) ->
                        cpdesc.text(data)
                    error:(xhr, status, err) ->
                      cpdesc.text("unknown")
                      
        initSemiproducts = ->
            spid = $(".semiprod-id").val()
            if(spid)
                spdesc = $(".semiproduct-desc")
                router.controllers.Semiproducts.getSemiproductDescription(spid).ajax
                    success:(data, status, xhr) ->
                        spdesc.text(data)
                    error:(xhr, status, err) ->
                        spdesc.text("unknonw")
        
        initOrderDefs = ->
            $(".order-def-id").each ->
                id = $(this).val()
                if(id)
                    desc = $(this).parent().find(".order-def-desc")
                    router.controllers.OrderController.orderDefDescription(id).ajax
                        success: (data, status, xhr) ->
                            desc.text(data)
                        error: (xhr, status, err) ->
                    	   desc.text("unknown")
        
        $(document).ready ->
            initCuttingPlans()
            initSemiproducts()
            odef.initDesc("order-def")
        
        repHandle = rep.addRepeated("part_handle")
        odef.register(repHandle, "order-def", $("#modal-order"), $("#modal-order-content"))
            
        $("#semiprod-button").click ->
            ss.selectSemiproduct($("#modal-sp"), $("#modal-sp-content"), $(".semiprod-id"), $(".semiproduct-desc"))
    )
)