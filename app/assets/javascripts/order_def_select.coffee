define(["jquery", "dataTables", "router"], ($, dt, router) ->
    idSelector = (handle) -> "."+handle+"_id"
    descSelector = (handle) -> "."+handle+"_desc"
    register : (rep, handle, modal, content) ->
        rep.added (ev) ->
            router.controllers.OrderController.list("table").ajax
                error: (xhr, status, err) ->
                    alert("Error getting orders: " +err)
                success: (data, status, xhr) ->
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
                                    ev.newItem.find(idSelector(handle)).val($(this).data("id"))
                                    ev.newItem.find(descSelector(handle)).text($(this).data("description"))
    initDesc : (handle) ->
        $(idSelector(handle)).each ->
            id = $(this).val()
            if(id)
                desc = $(this).parent().find(descSelector(handle))
                router.controllers.OrderController.orderDefDescription(id).ajax
                    success: (data, status, xhr) ->
                        desc.text(data)
                    error: (xhr, status, err) ->
                       desc.text("unknown")
)