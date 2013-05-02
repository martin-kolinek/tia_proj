define(["jquery", "router", "dataTables", "bootstrap"], ($, router, dt, bs) ->
    idSelector = (handle) -> "."+handle+"_id"
    descSelector = (handle) -> "."+handle+"_desc"
    register: (rep, modal, content, handle) ->
    	rep.added((ev) ->
            rep.disableAdding()
            router.controllers.PartDefinitions.selectList().ajax
                success: (data, status, xhr) ->
                    content.html(data)
                    content.find("tbody tr").addClass("pointer")
                    content.find("tbody tr").click ->
                        id = $(this).data("id")
                        desc = $(this).data("description")
                        ev.newItem.find(idSelector(handle)).val(id)
                        ev.newItem.find(descSelector(handle)).html(desc)
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
        $(document).ready ->
    	   $(descSelector(handle)).each ->
                id = $(this).parent().find(idSelector(handle)).val()
                if(!id)
                   return
                div = $(this)
                router.controllers.PartDefinitions.partDefDescription(id).ajax
                    success: (data, status, xhr) ->
                        div.html(data)
                    error: (xhr, status, err) ->
                        div.html("unknown")
)