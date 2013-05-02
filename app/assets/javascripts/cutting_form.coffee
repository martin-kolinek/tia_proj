require(["common"], (common) ->
    require(["jquery", "semiproduct_select", "form_repeating", "router"], ($, ss, rep, router) ->
        handle = rep.addRepeated("part_handle")
        $(document).ready ->
            cpdesc = $(".cutplan-desc")
            cpid = $(".cutplan-id").val()
            if(cpid)
                router.controllers.CuttingPlans.cutPlanDescription(cpid).ajax
                    success:(data, status, xhr) ->
                        cpdesc.text(data)
            	    error:(xhr, status, err) ->
        	          cpdesc.text("unknown")
            spid = $(".semiprod-id").val()
            if(spid)
                spdesc = $(".semiproduct-desc")
                router.controllers.Semiproducts.getSemiproductDescription(spid).ajax
                    success:(data, status, xhr) ->
                        spdesc.text(data)
                    error:(xhr, status, err) ->
                    	spdesc.text("unknonw")
        $("#semiprod-button").click ->
            ss.selectSemiproduct($("#modal-sp"), $("#modal-sp-content"), $(".semiprod-id"), $(".semiproduct-desc"))
    )
)