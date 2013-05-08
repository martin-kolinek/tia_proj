define(["jquery", "dataTables", "router", "id_desc"], ($, dt, router, iddesc) ->
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
                                    ev.newItem.find(iddesc.id(handle)).val($(this).data("id"))
                                    ev.newItem.find(iddesc.desc(handle)).text($(this).data("description"))
    initDesc : (handle) ->
        iddesc.init(handle, router.controllers.OrderController.orderDefDescription)
)