require(["common"], (common) ->
	require(["jquery", "form_repeating", "order_def_select", "router", "dataTables", "id_desc", "bootstrap"], ($, rep, odef, router, dt, iddesc, bs) ->
        repHandle = rep.addRepeated("parthandle")
        odef.initDesc("odefhandle")
        repHandle.added (ev) ->
            repHandle.disableAdding()
            odefid = ev.newItem.parents(".odef-wrapper").find(iddesc.id("odefhandle")).val()
            router.controllers.PartDefinitions.listFinishedParts(odefid, "table").ajax
                success: (data, status, xhr) ->
                    content = $("#part-modal-content")
                    modal = $("#part-modal") 
                    content.html(data)
                    content.find("table").dataTable()
                    content.find("tbody tr").addClass("pointer")
                    content.find("tbody tr").click ->
                        material = $(this).data("mat")
                        shape = $(this).data("shape")
                        if(!material)
                            modal.modal("hide")
                            return
                        matDesc = $(this).data("mat-desc")
                        shapeDesc = $(this).data("shape-desc")
                        ev.newItem.find(iddesc.id("mathandle")).val(material)
                        ev.newItem.find(iddesc.desc("mathandle")).text(matDesc)
                        ev.newItem.find(iddesc.id("shphandle")).val(shape)
                        ev.newItem.find(iddesc.desc("shphandle")).text(shapeDesc)
                        modal.off("hidden")
                        modal.modal("hide")
                        repHandle.enableAdding()
                    modal.on("hidden", ->
                        ev.newItem.remove()
                        repHandle.enableAdding()
                    )
                    modal.modal("show")
                error: (xhr, status, err) ->
                    alert("Error retrieving parts: "+err)
        iddesc.init("mathandle", router.controllers.Semiproducts.materialDescription)
        iddesc.init("shphandle", router.controllers.Semiproducts.basicShapeDescription)
    )
)