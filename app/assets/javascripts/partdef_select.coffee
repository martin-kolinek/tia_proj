define(["jquery", "router", "dataTables", "bootstrap", "id_desc"], ($, router, dt, bs, iddesc) ->
    register: (rep, modal, content, handle) ->
    	rep.added((ev) ->
            rep.disableAdding()
            router.controllers.PartDefinitions.list("table").ajax
                success: (data, status, xhr) ->
                    content.html(data)
                    content.find("tbody tr").addClass("pointer")
                    content.find("tbody tr").click ->
                        id = $(this).data("id")
                        desc = $(this).data("description")
                        ev.newItem.find(iddesc.id(handle)).val(id)
                        ev.newItem.find(iddesc.desc(handle)).html(desc)
                        rep.enableAdding()
                        modal.off("hidden")
                        modal.modal("hide")
                    content.find("table").dataTable()
                    modal.on("hidden", ->
                        ev.newItem.remove()
                        rep.enableAdding()
                    )
                    modal.modal("show")
                error: (xhr, status, err) ->
                    rep.enableAdding()
                    alert("Error getting part definitions for selection: "+err)
        )
    initDesc: (handle) ->
    	iddesc.init(handle, router.controllers.PartDefinitions.partDefDescription)
)