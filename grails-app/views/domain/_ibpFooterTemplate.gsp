<style>

#ibp-footer {
background-color: #ECE9B7;
color: #5E5E5E;
font-family: Verdana,Helvetica,Sans-Serif;
height: 80px;
width: 100%;
z-index: 2000;
position: relative;
border-top: 1px solid #e5e5e5;          
}
.gradient-bg-reverse {
   background-color: #ECE9B7;
   background-image: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#ebfefd), to(#ECE9B7));
   background-image: -webkit-linear-gradient(left, #ebfefd, #ECE9B7);
   background-image:    -moz-linear-gradient(left, #ebfefd, #ECE9B7);
   background-image:     -ms-linear-gradient(left, #ebfefd, #ECE9B7);
   background-image:      -o-linear-gradient(left, #ebfefd, #ECE9B7);
}
#ibp-footer ul {
list-style: none;
position: absolute;
bottom: 20px;
right: 20px;        
}

#ibp-footer li {
display: inline;
cursor: pointer;         
}
</style>

<div id="ibp-footer" class="gradient-bg-reverse" style="display:none;">
    <ul>
    <li onclick="location.href='/terms'" title="Terms">Terms</li>
    <li onclick="location.href='/license'" title="Licenses">Licenses</li>
    <li onclick="location.href='/feedback'" title="Feedback">Feedback</li>
    </ul>
</div>
