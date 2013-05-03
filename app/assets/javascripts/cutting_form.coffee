require(["common"], (common) ->
    require(["jquery", "semiproduct_select", "form_repeating", "router"], ($, ss, rep, router) ->
    	
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
        
        $(document).ready ->
            initCuttingPlans()
            initSemiproducts()
        
        handle = rep.addRepeated("part_handle")
        handle.added (ev) ->
            router.controllers.OrderController.list("table").ajax
                error: (xhr, status, err) ->
                    alert("Error getting orders: " +err)
                success: (data, status, xhr) ->
                    content = $("#modal-order-content")
                    modal = $("#modal-order") 
                    content.html(data)
                    content.find("table").dataTable()
                    modal.on("hidden", ->
                        ev.newItem.remove()
                    )
                    modal.modal("show")
                    content.find("a").click ->
                        ordid = $(this).data("ord-id")
                        ordname = $(this).data("ord-name")
                        ul = $(this).parent().find("ul")
                        router.controllers.OrderController.listDefinitions(ordid,"dropdown").ajax
                            error: (xhr, status, err) ->
                                alert("Error getting order definitions: "+err)
                            success: (data, status, xhr) ->
                                ul.html(data)
                                ul.find("a").click ->
                                    modal.off("hidden")
                                    modal.modal("hide")
                                    ev.newItem.find(".order-def-id").val($(this).data("id"))
                                    ev.newItem.find(".order-def-desc").text($(this).data("description"))
            
        $("#semiprod-button").click ->
            ss.selectSemiproduct($("#modal-sp"), $("#modal-sp-content"), $(".semiprod-id"), $(".semiproduct-desc"))
    )
)