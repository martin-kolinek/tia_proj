require(["common"], (common) ->
    require(["jquery", "form_repeating", "router", "temp_files", "bootstrap", "dataTables"], ($, repeat, router, tf, bs, dt) ->
        tf.create("cutplanfile")
        repHandle = repeat.addRepeated("pd_handle")
        repHandle.added((ev) ->
            repHandle.disableAdding()
            router.controllers.PartDefinitions.selectList().ajax
                success: (data, status, xhr) ->
                    modal = $("#part_def_modal")
                    content = $("#part_def_modal_content")
                    content.html(data)
                    content.find("tbody tr").addClass("pointer")
                    content.find("tbody tr").click ->
                        id = $(this).data("id")
                        desc = $(this).data("description")
                        ev.newItem.find(".pdef-id-hf").val(id)
                        ev.newItem.find(".pdef-desc").html(desc)
                        repHandle.enableAdding()
                        modal.off("hidden")
                        modal.modal("hide")
                    content.find("table").dataTable()
                    modal.on("hidden", ->
                        ev.newItem.remove()
                        repHandle.enableAdding()
                    )
                    modal.modal("show")
                error: (xhr, status, err) ->
                    repHandle.enableAdding()
                    alert("Error getting part definitions for selection: "+err)
        )
        $(document).ready ->
            $(".pdef-desc").each ->
        	    id = $(this).parent().find(".pdef-id-hf").val()
        	    if(!id)
        	       return
        	    div = $(this)
        	    router.controllers.PartDefinitions.partDefDescription(id).ajax
        	        success: (data, status, xhr) ->
                        div.html(data)
                    error: (xhr, status, err) ->
                	    div.html("unknown")
    )
)