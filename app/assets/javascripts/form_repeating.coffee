define(["jquery"], ($) ->
    addRepeated:(handle) ->
        btn = $("#"+handle+"_add")
        template = $("#"+handle+"_template")
        key = btn.data("key")
        itemClass = handle+"_item"
        removeButtonClass = handle+"_rem"
        ret = 
            added : (handler) ->
                $(ret).on("added", handler)
            disableAdding: ->
                btn.off("click")
            enableAdding: ->
            	btn.click ->
                    newItem = template.clone(true)
                    newItem.addClass(itemClass)
                    template.before(newItem)
                    
                    evnt = $.Event("added")
                    evnt.newItem = newItem
                    newItem.show()
                    renumber()
                    $(ret).trigger(evnt)	
        renumber = ->
            rgx = new RegExp(key+"""\\[.+\\]""", "g")
            $("."+itemClass).each (index, element) =>
                $(element).find("[name*='"+key+"']").each ->
                    $(this).prop("name", $(this).prop("name").replace(rgx, key+"["+index+"]"))
        remove = (event) ->
            $(this).parents("."+itemClass).remove()
            renumber()
        $("document").ready ->
            ret.enableAdding()
            $("."+removeButtonClass).on("click", remove)
        ret
)