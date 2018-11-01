$(document).ready(

function(){

    // no libraries used for the signup form! :D ;)

    let user;

    function parseJwt (token) {
        var base64Url = token.split('.')[1];
        var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(window.atob(base64));
    };

    //default values for forms
        let default_values = {
            name : $("#name").val(),
            email : $("#email").val(),
            allValues : [$("#name").val(),$("#email").val()]
        };

        let formEvents = {
            source : '',    //for magic purposes
            clicks : 0,

            _isValidForm : function (formParams) {

                let rules = {
                    name : function(funcContainingRulesFornaming,params){
                        return funcContainingRulesFornaming.call(rules,params);
                    },

                    email : function(email){
                        let email_regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
                        if(email_regex.test(email)){
                            console.log("email validator passed");
                            return [true];
                        }

                        return [false];
                    }
                }

                //write rules here
                let formTests = {
                    rules : {
                        email : 'Email needs to be a valid one',
                        name : 'Name must be greater than 3 letters\nName cannot contain default values'
                    },
                    getRules(param){
                      return this.rules[param];
                    },
                    getTestParams(){
                        //hardcoded for now -
                        return Object.keys(this).filter(key => {
                            return (key !== 'rules' && !key.startsWith("get"));
                        })
                    },
                    email : rules.email(formParams["email"]),
                    //define your custom rules here - implement the same for email fields
                    name  : rules.name(function(name){
                        if(name.length > 3 && !name.includes(default_values["name"])){
                            console.log("name validator passed!");
                            return [true];
                        }
                        return [false];
                    },formParams["name"])
                };

                if(formParams["email"] && formParams["name"]){

                    //passed integrity check
                    if(formTests.email && formTests.name){
                        return ["ok",""];
                    }

                    //fail : contains default value || user has not entered any values on his own
                    else if(formParams.email === default_values.email|| formParams.name === default_values.name){
                        console.log("contains default value(s), form integrity failed");

                        //fields with default values
                        let defaultParams = [];
                        defaultParams.push("Contains default parameters");
                        for(const param of Object.keys(formParams)){
                            //current form value == default set form value
                            if(formParams[param] === default_values[param]){
                                defaultParams.push(param);
                            }
                        }
                        return defaultParams;
                    }

                    //fail : one of the rule(s) are not satisfied
                    else{
                        //invalid message
                        let invalidParams = [];
                        let messageString = "";

                        for(const test of formTests.getTestParams()){
                            // console.log(formTests[test]);
                            let testResult = formTests[test];
                            console.log(`${test} is ${testResult[0]} and results : ${testResult[1]}`);
                            if(!testResult[0]){
                                invalidParams.push(test);
                                messageString += "\n"+testResult[1];
                            }
                        }
                        console.log("true check for validation",invalidParams);
                        invalidParams.unshift("Rules not satisfied: \n"+messageString);
                        return invalidParams;
                    }
                }

                else{
                    //fail : param(s) are empty
                    let emptyParams = [];
                    emptyParams.push("required params empty");
                    for(const test of Object.keys(formTests)){
                        console.log(formTests[test]);
                        if(!formTests[test]){
                            emptyParams.push(test);
                        }
                    }
                    return emptyParams;
                }
            },

            submit : function (formParams,otherparams,actionsuponinvalidparams) {

                if(!formParams){
                    return false;
                }

                let validatorResult = this._isValidForm(formParams);
                if(Array.isArray(validatorResult)){
                    let message = validatorResult[0];
                    if(message === "ok"){
                        actionsuponinvalidparams(message,[]);
                        //do the fetch post request bla bla
                    }

                    else{
                        let problematic_params = validatorResult.splice(1,validatorResult.length);
                        console.log(message,problematic_params);
                        actionsuponinvalidparams(message,problematic_params);
                    }
                }else {
                    //will never happen unless the source is manually tampered
                    console.log("validator response isn't an array");
                }

            }
        };

        //magic code that does lot of magic
        $("body").on("click",function(evt){
            evt.stopImmediatePropagation();

            if($(evt.target).is("input")){

                formEvents.clicks++;
          
                let event = evt;
                let _formEvents = formEvents;
                let _defaultObj = default_values.allValues;

                setTimeout(function(){
                    let value = $(event.target).val();
               
                    console.log("refactor check: ",_formEvents,"**",_defaultObj);

                    if(event.type === "click" &&
                        _formEvents.clicks < 2 && _formEvents.clicks > 0 &&
                        _defaultObj.includes(value)) {

                        formEvents.source = $(event.target).attr("id");
                        $(event.target).val("");
                    }
                    console.log(_defaultObj)
                    formEvents.clicks = 0;
                },300);

            }

            else{
             
                $("#signup-form input").each(function(index){
                    if($(this).val().length <= 0){
                        setTimeout(()=>{
                            $(this).val(default_values[$(this).attr("id")]);
                        },250);
                    }
                })
            }
        });

        $("body").on("keydown",function(evt){

            if(evt.metaKey && evt.keyCode === 13 || evt.ctrlKey){
                console.log('command and enter is pressed');
                console.log("sign up from submitted using cmd+enter");

                let formParams = {
                    email : $("#email").val(),
                    name : $("#name").val()
                }
                formEvents.submit(formParams,'',function(message,invalidParams){
                 
                    invalidParams.forEach(param => {
                        $("#"+param).closest(".signup-field").addClass('highlight-field-error');
                        let invalidParamEl =  $("#"+param).closest(".signup-field");
                        setTimeout(()=>{
                            invalidParamEl.removeClass('highlight-field-error');
                        },500);
                      
                    });

                    $.alert({
                        title : false,
                        escapeKey : true,
                        boxWidth : '50%',
                        theme : 'dark',
                        useBootstrap: false,
                        content : message,
                        autoClose : 'close|5000',
                        buttons : {
                            close : {
                                isHidden : true,
                            }
                        },
                        backgroundDismiss : true,
                    });
                });
                //submit the form
            }
        });

        $("body").on("click","#submit-signup-form",function(evt){

            console.log("submit attempted using click");

                let formParams = {
                    email : $("#email").val(),
                    name : $("#name").val()
                }
                formEvents.submit(formParams,'',function(message,invalidParams){

                    //assign invalid css to invalid parameters
                    invalidParams.forEach(param => {
                        $("#"+param).closest(".signup-field").addClass('highlight-field-error');
                        let invalidParamEl =  $("#"+param).closest(".signup-field");
                        setTimeout(()=>{
                            invalidParamEl.removeClass('highlight-field-error');
                        },500);
                      
                    });

                    $.alert({
                        title : false,
                        escapeKey : true,
                        boxWidth : '50%',
                        theme : 'dark',
                        useBootstrap: false,
                        content : message,
                        buttons : {
                            close : {
                                isHidden : true,
                            }
                        },
                        backgroundDismiss : true,
                    });

                    let failed_signup = $.alert({
                        lazyOpen : true,
                        title : false,
                        escapeKey : false,
                        boxWidth : '50%',
                        theme : 'dark',
                        useBootstrap: false,
                        content : "Signup failed",
                        autoClose : 'close|2500',
                        buttons : {
                            close : {
                                isHidden : true,
                            }
                        },
                        backgroundDismiss : true,
                    });

                    let successful_signup = $.alert({
                        lazyOpen : true,
                        title : false,
                        escapeKey : false,
                        boxWidth : '50%',
                        theme : 'dark',
                        useBootstrap: false,
                        content : "Signup success. Logging you in ... ",
                        autoClose : 'close|2500',
                        buttons : {
                            close : {
                                isHidden : true,
                            }
                        },
                        backgroundDismiss : true,
                    });

                    if(message === 'ok'){
                        let signUpUrl = "/auth/signup/google";

                        let signupParams = {
                            name : $("#name").val(),
                            email : $("#email").val()
                        };

                        let init = {
                            method : 'POST',
                            headers : {
                                'Content-Type' : 'application/json',
                            },
                            body : JSON.stringify(signupParams),
                        };

                        fetch(signUpUrl,init).then(signupResponse => {
                            return signupResponse.json();
                        }).then(token => {
                            if(token.ok){
                                let userTokenJson = token.message.googleToken;
                                let userToken = JSON.parse(userTokenJson);
                                window.localStorage.setItem('user',userTokenJson);
                                successful_signup.open();
                                return userToken;
                            }
                            else{
                                failed_signup.open();
                                return token.message.reason;
                            }
                        }).then(finalres => {
                            setTimeout(()=>{
                                window.location = "/";
                            },4000);
                        })
                    }

                });
                //submit the form

        });

        $("#login-signup").click(()=>{
            boxes.showSignupBox();
        });

        $("body").on("click","#login",function(){
            $(".jconfirm").remove();
            boxes.showLoginBox();
        });
        $("body").on("click","#signup",function(){
            $(".jconfirm").remove();
            boxes.showSignupBox();
        });

        var boxes = {

            showSignupBox : function(){
                $.confirm({
                    title : false,
                    escapeKey : true,
                    theme : 'material',
                    animationSpeed: 400,
                    boxWidth : '30%',
                    useBootstrap : false,
                    content : '<div class = "login-signup-box">\n' +
                    '        <div class="title">Continue with </div>\n' +
                    '        <div id="social-login">\n' +
                    '            <img src="/image/googe-icon.png" id="google-sign-in" class="social-signin gsign-in" onclick="authoriseWithGoogle()">\n' +
                    '        </div>\n' +
                    '    </div>',
                    buttons : {
                        close : {
                            isHidden : true
                        }
                    },
                    backgroundDismiss : true,

                });
            },

            showLoginBox : function(){
                $.confirm({
                    title : false,
                    escapeKey : true,
                    theme : 'material',
                    animationSpeed: 400,
                    boxWidth : '30%',
                    useBootstrap : false,
                    content : '<div class = "login-signup-box">\n' +
                    '        <div class="title">Login with </div>\n' +
                    '        <div id="social-login">\n' +
                    '            <img src="/image/googe-icon.png" id="google-sign-in" class="social-signin gsign-in" onclick="authoriseWithGoogle()">\n' +         
                    '        </div>\n' +
                    '        <div id ="signup" class="new-user" >Need an account? Sign up</div>\n' +
                    '    </div>',
                    buttons : {
                        close : {
                            isHidden : true
                        }
                    },
                    backgroundDismiss : true,

                });
            },

            showSignupForm : function (partialObject) {

                let user_object = partialObject;
                $(".jconfirm").remove();
                $("#signup-form").remove();
                $(".login-signup-box").remove();
                
					
                if(user_object.name && user_object.email) {

                    let form = ' <div id = "signup-form" onsubmit="return false;">\n' +
                        '        <h2 class="title">Sign up</h2>\n' +
                        '        <form id = "create-issue-popup-form" autocomplete="off" action="#">\n' +
                        '\n' +
                        '            <!-- name -->\n' +
                        '            <div class = "signup-field">\n' +
                        '                <div class="textInput">\n' +
                        '                    <div class="inset">\n' +
                        '                        Name\n' +
                        '                    </div>\n' +
                        '                    <div class="inputContainer">\n' +
                        `                        <input id="name" type="text" placeholder="required" value=${user_object.name} autocomplete="off">` +
                        '                    </div>\n' +
                        '                </div>\n' +
                        '            </div>\n' +
                        '            <!-- email -->\n' +
                        '            <div class="signup-field">\n' +
                        '                <div class="textInput">\n' +
                        '                    <div class="inset">\n' +
                        '                        Email\n' +
                        '                    </div>\n' +
                        '                    <div class="inputContainer">\n' +
                        `                        <input id="email"  type="text" placeholder="required" value=${user_object.email}>\n` +
                        '                    </div>\n' +
                        '                </div>\n' +
                        '            </div>\n' +
                        '\n' +
                        '            <div class="create-issue-popup-done">\n' +
                        '                <button id ="submit-signup-form" class="create-post-btn">Signup</button>\n' +
                        '            </div>\n' +
                        '        </form>\n' +
                        '    </div>';


                    $.confirm({
                        title : false,
                        escapeKey: false,
                        theme: 'material',
                        animationSpeed: 400,
                        boxWidth: '70%',
                        useBootstrap: false,
                        content: form,
                        buttons: {
                            close: {
                                isHidden: true
                            }
                        },
                        backgroundDismiss: false,

                    });

                    setTimeout(()=>{
                        let fieldsPresent = [];
                        //pushing all the fields in the form to check against the received ones from
                        //google signup flow
                        $("#signup-form input").each(function(index){
                            fieldsPresent.push($(this).attr("ID"));
                        });

                        let prompt_field = '';
                        for(const defaultParams of fieldsPresent){
                            console.log((fieldsPresent));
                            if(user_object[defaultParams]){
                                $("#"+defaultParams).closest(".signup-field").addClass('highlight-field-ok');
                                $("#"+defaultParams).closest(".signup-field").addClass('set-focus-die');
                                $("#"+defaultParams).attr("readonly",true);
                                // $("#"+defaultParams).off("click focus");
                            }
                            else {
                                $("#"+defaultParams).closest(".signup-field").addClass('highlight-field-error');
                                prompt_field = $("#"+defaultParams);
                            }
                        }

                        setTimeout(()=>{
                            if(prompt_field){
                                prompt_field.click();
                                prompt_field.focus();
                                prompt_field.closest(".signup-field").removeClass('highlight-field-error');
                            }
                        },500);

                    },150);

                }
            }
        }

        // boxes.showSignupForm({});
        if(getCookie("user_details") && !user){
            let user_jwt_for_signup = getCookie("user_details");
            user = parseJwt(user_jwt_for_signup);
            boxes.showSignupForm(user);

        }
    }
)