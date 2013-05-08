require(["common"], (common) ->
	require(["jquery", "form_repeating", "order_def_select", "router", "dataTables", "id_desc"], ($, rep, odef, router, dt, iddesc) ->
        repHandle = rep.addRepeated("parthandle")
        odef.initDesc("odefhandle")
        repHandle.added (ev) ->
            repHandle.disableAdding()
            router.controllers.PartDefinitions.listFinishedParts("table").ajax
                success: (data, status, xhr) ->
                    content = $("#part-modal-content")
                    modal = $("part-modal") 
                    content.html(data)
                    content.find("table").dataTable()
                    content.find("tbody tr").addClass("pointer")
                    content.find("tbody tr").click ->
                        material = $(this).data("mat-id")
                        shape = $(this).data("shape-id")
                        matDesc = $(this).data("mat-desc")
                        shapeDesc = $(this).data("shape-desc")
                        ev.newItem.find(iddesc.id("mathandle")).val(material)
                        ev.newItem.find(iddesc.desc("mathandle")).text(matDesc)
                        ev.newItem.find(iddesc.id("shphandle")).val(shape)
                        ev.newItem.find(iddesc.desc("shphandle")).text(shapeDesc)
                        modal.off("hidden")
                        modal.modal("hide")
                    modal.on("hidden", ->
                        ev.newItem.remove()
                    )
                    modal.modal("show")
                error: (xhr, status, err) ->
                    alert("Error retrieving parts: "+err)
        iddesc.init("mathandle", router.controllers.Semiproducts.materialDescription)
        iddesc.init("shphandle", router.controllers.Semiproducts.basicShapeDescription)
    )
)