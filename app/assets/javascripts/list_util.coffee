define(["underscore"], ->
    listDt: (rows) ->
        bFilter:false
        bLengthChange:false
        aoColumns: _.map([1...rows], (x) -> null).concat({bSortable:false})
)