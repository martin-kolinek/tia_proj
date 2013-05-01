define(["jquery", "router"], ($, router) ->
    register: (rep, modal, content, idClass, descClass) ->
    	rep.added((ev) ->
            rep.disableAdding()
            router.controllers.PartDefinitions.selectList().ajax
                success: (data, status, xhr) ->
                    content.html(data)
                    content.find("tbody tr").addClass("pointer")
                    content.find("tbody tr").click ->
                        id = $(this).data("id")
                        desc = $(this).data("description")
                        ev.newItem.find("."+idClass).val(id)
                        ev.newItem.find("."+descClass).html(desc)
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
    initDesc: (idClass, descClass) ->
        $(document).ready ->
    	   $("."+descClass).each ->
                id = $(this).parent().find("."+idClass).val()
                if(!id)
                   return
                div = $(this)
                router.controllers.PartDefinitions.partDefDescription(id).ajax
                    success: (data, status, xhr) ->
                        div.html(data)
                    error: (xhr, status, err) ->
                        div.html("unknown")
)