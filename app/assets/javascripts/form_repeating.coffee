define(["jquery"], ($) ->
    addRepeated:(addBtn) ->
        btn = $("#"+addBtn)
        template = $("#"+btn.data("template"))
        key = btn.data("key")
        itemClass = template.data("item-class")
        removeButtonClass = template.data("remove-class") 
        renumber = ->
            rgx = new RegExp(key+"""\\[.+\\]""", "g")
            $("."+itemClass).each (index, element) =>
                $(element).find("[name*='"+key+"']").each ->
                    $(this).prop("name", $(this).prop("name").replace(rgx, key+"["+index+"]"))
        remove = (event) ->
            $(this).parents("."+itemClass).remove()
            renumber()
        $("document").ready ->
            btn.click ->
                newItem = template.clone(true)
                newItem.addClass(itemClass)
                template.before(newItem)
                newItem.show()
                renumber()
            $("."+removeButtonClass).on("click", remove)
)