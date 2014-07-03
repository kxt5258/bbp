$("#addBulkObservationsSubmit").click(function(event){
    if($(this).hasClass('disabled')) {
        alert("Uploading is in progress. Please submit after it is over.");
        event.preventDefault();
        return false; 		 		
    }

    if (document.getElementById('agreeTerms').checked) {
        var me = this;
        $(me).addClass("disabled");

        var allForms = $(".addObservation");
        var formsWithData = [] 
        $.each(allForms, function(index, value){
            if(formHasData(value)){
                console.log("==============================================");
                formsWithData.push(value);
            }
        });
        var size = formsWithData.length;
        submitForms(0, size, formsWithData); 
        $(me).removeClass("disabled");
        return false;
    } else {
        alert("Please agree to the terms mentioned at the end of the form to submit the observation.");    
        $(me).removeClass("disabled");
    }
});

function formHasData(form){
    if($(form).find(".createdObv").is(":visible")) {
        return false;
    }
    if($(form).find(".imageHolder .addedResource").length != 0){
        return true;
    }
    var inputNames  = ['habitat_id', 'group_id', 'fromDate']
    var flag = false;
    $.each(inputNames, function(index, value){   
        if($(form).find("input[name='"+value+"']").val() != ""){
            flag = true;
            return;
        }
    });
    return flag;
}

var gotError = false;
function submitForms(counter, size, allForms){
    if(counter == size){
        console.log("breaking recursion========" + gotError);
        if(!gotError){
            location.reload();
        }
        return;
    } else {
        console.log("going to submit form no : " + counter);
        var form = allForms[counter];
        $(form).find(".userGroupsList").val(getSelectedUserGroups($(form)));
           
        var locationpicker = $(form).find(".map_class").data('locationpicker'); 
        if(locationpicker.mapLocationPicker.drawnItems) {
            var areas = locationpicker.mapLocationPicker.drawnItems.getLayers();
            if(areas.length > 0) {
                var wkt = new Wkt.Wkt();
                wkt.fromObject(areas[0]);
                $(form).find("input.areas").val(wkt.write());
            }
        }
        var imagesPulled = $(form).find(".imageHolder li.addedResource");
        var group_id = $(form).find("input[name='group_id']").val();
        var habitat_id = $(form).find("input[name='habitat_id']").val();
        $(form).ajaxSubmit({
            url : $(this).attr("action"),
            dataType : 'json', 
            type : 'POST',
            success : function(data, statusText, xhr, form) {
                console.log("HERE HERE");
                if(data.statusComplete) {
                    console.log("HELLO IN SUCCESS");
                    $(form).find('input').attr('disabled', 'disabled');
                    $(form).find('button').attr('disabled', 'disabled');
                    $(form).find('.span4').css('opacity', '0.6');
                    $(form).find('.createdObv').show();
                    //disable click on div
                } else {
                    gotError = true;
                    var miniObvCreateHtml = data.miniObvCreateHtml;
                    var wrapper = $(form).parent();
                    $(form).replaceWith(miniObvCreateHtml);
                    console.log($(miniObvCreateHtml));
                    $(wrapper).find(".imageHolder").append(imagesPulled);
                    $(wrapper).find(".group_options li[value='"+group_id+"']").trigger("click");
                    $(wrapper).find(".habitat_options li[value='"+habitat_id+"']").trigger("click");
                }
                submitForms(counter+1, size, allForms);
            }, error : function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                console.log("ERROR ERROR");
                var successHandler = this.success;
                handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
                    var response = $.parseJSON(xhr.responseText);
                });
                submitForms(counter+1, size, allForms);
            }  
        });
    }
}

function dropAction(event, ui, ele) {
    console.log("Item was Dropped");
    $(ele).append($(ui.draggable).clone());
    console.log($(ui.draggable));
    var $ratingCont = $(ele).find(".star_obvcreate");
    console.log($ratingCont);
    rate($ratingCont);
    var imageID = $(ui.draggable).find("img").first().attr("id");
    $("#"+imageID).mousedown(function(){console.log("mouse down");return false;});
    $(ui.draggable).css("opacity","0.3");

}

$(".obvCreateTags").tagit({
    select:true, 
    allowSpaces:true, 
    placeholderText:'Add some tags',
    fieldName: 'tags', 
    autocomplete:{
        source: '/observation/tags'
    }, 
    triggerKeys:['enter', 'comma', 'tab'], 
    maxLength:30
});

$(".applyAll").click(function(){
    var licenseVal = $(".propagateLicense").find("input").val();
    var dateVal = $(".propagateDate").find("input[name='fromDate']").val();
    var tagValues = []
    $('.propagateTags span.tagit-label').each(function(i){
        tagValues.push($(this).text()); // This is your rel value
    });
    var groups = getSelectedUserGroups($(".propagateGroups"));
    console.log(groups);
    var latVal = $(".propagateLocation").find(".latitude_field").val();
    var longVal = $(".propagateLocation").find(".longitude_field").val();
    var allForms = $(".addObservation");
    $.each(allForms, function(index,value){
        $(value).find(".imageHolder li span:contains('"+licenseVal+"')").first().trigger("click");
        $(value).find('.fromDate').datepicker("setDate", dateVal);
        $.each(tagValues, function(index, tagVal){
            $(value).find(".obvCreateTags").tagit("createTag", tagVal);//select ul in this and create new tags//createTag
        });
        $.each(groups, function(index, grpVal){
            var sel = ".userGroupsClass .checkbox button[value='"+grpVal+"']"
            $(value).find(sel).trigger("click");//select ul in this and create new tags//createTag
        });
        $(value).find(".latitude_field").val(latVal);
        $(value).find(".longitude_field").val(longVal);
        var e = $.Event("keypress"); /* || keydown || keyup */
        e.which = 13; /* set the key code here */
        $(value).find(".latitude_field").trigger(e);
        $(value).find(".longitude_field").trigger(e);
    });
});
