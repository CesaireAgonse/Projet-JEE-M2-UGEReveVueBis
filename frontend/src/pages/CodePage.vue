<template>
  <div class="col">
    <button class="basic-button left limit" @click="home">
      <i>
        <i class="fa-solid fa-arrow-left"></i>
        Home
      </i>
    </button>
    <div class="row">
      <div class="post">
        <CodeVisual :post="post" v-if="post != null" @code-selected="updateSelectedCode" @refresh="refresh"></CodeVisual>
      </div>
      <div v-if="post != null && post.unitContent != null && post.unitContent !== ''" class="post">
          <CodeTestVisual :post="post"></CodeTestVisual>
      </div>
      <div class="comments" v-if="post != null">
        <h2>Commentaires:</h2>
        <div v-for="comment in commentsPage" :key="comment">
          <div class="postContent">
            <CommentVisual  :comment="comment" @refresh="refresh"></CommentVisual>
          </div>
        </div>
        <div class="row">
          <button v-if="pageNumber > 0" class="basic-button prevButton" @click="commentsPrev">
              <i class="fa-solid fa-arrow-left"></i>
          </button>
          <button v-if="pageNumber < maxPageCommentNumber" class="basic-button nextButton" @click="commentsNext">
              <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>
        <div v-if="auth != null">
          <button type="button" @click="addSelectedCode" class="basic-button add">Ajouter le code selectionné</button>
          <div v-if="selectedCode !== ''" class="basic-button cross-button right" @click="removeSelection"> <i class="fa-solid fa-xmark"></i></div>
          <pre v-if="selectedCode !== ''" class="select-code"><code class="language-java">{{ selectedCode }}</code></pre>
          <div class="row">
            <textarea v-model="contentTextarea" placeholder="Entrez votre texte ici"></textarea>
            <div class="send-button" @click="comment()">
              <i class="fa-regular fa-paper-plane fa-2xl" style="color: #ffffff;"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="reviews" v-if="post != null">
      <h2>Reviews:</h2>
      <button v-if="sortBy !== 'newest'" class="basic-button" @click="newest">Les plus récents</button>
      <button v-if="sortBy === 'newest'" class="select-button" @click="newest">Les plus récents</button>
      <button v-if="sortBy !== 'relevance'" class="basic-button" @click="relevance">Les mieux notés</button>
      <button v-if="sortBy === 'relevance'" class="select-button" @click="relevance">Les mieux notés</button>
      <div v-for="review in reviewsPage" :key="review">
        <div class="postContent">
          <ReviewVisual  :post="review" @refresh="refresh"></ReviewVisual>
        </div>
      </div>
      <div class="row">
        <button v-if="pageReviewNumber > 0" class="basic-button prevButton" @click="reviewsPrev">
            <i class="fa-solid fa-arrow-left"></i>
        </button>
        <button v-if="pageReviewNumber < maxPageReviewNumber" class="basic-button nextButton" @click="reviewsNext">
            <i class="fa-solid fa-arrow-right"></i>
        </button>
      </div>
    </div>
    <div v-if="auth != null">
      <ReviewForm @refresh="reviews"></ReviewForm>
    </div>
  </div>
</template>

<script>
import CodeVisual from "@/visuals/CodeVisual.vue";
import CommentVisual from "@/visuals/CommentVisual.vue";
import CodeTestVisual from "@/visuals/CodeTestVisual.vue";
import ReviewVisual from "@/visuals/ReviewVisual.vue";
import { codeService } from "@/services/code.service";
import { postService } from "@/services/post.service";
import router from "@/router";
import { library, dom } from "@fortawesome/fontawesome-svg-core";
import { fas } from '@fortawesome/free-solid-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons';
import { far } from '@fortawesome/free-regular-svg-icons';
import Prism from 'prismjs';
import "prismjs/themes/prism-tomorrow.css"
import 'prismjs/components/prism-java'
import {authenticationService} from "@/services/authentication.service";
import ReviewForm from "@/components/ReviewForm.vue";
library.add(fas, far, fab)
dom.watch();

export default {
  components: {CodeVisual, CodeTestVisual, CommentVisual, ReviewVisual, ReviewForm},
  mounted() {
    document.title = "Code"
    this.comments()
    this.reviews()
    this.code()
    document.body.style.overflowY = "visible"
  },
  data() {
    return {
      post: null,
      contentTextarea: '',
      reviewTextarea:'',
      title:'',
      selectedCodePre:'',
      selectedCode:'',
      commentsPage:null,
      pageNumber:0,
      maxPageCommentNumber:0,
      maxPageReviewNumber:0,
      reviewsPage:null,
      sortBy: 'newest',
      pageReviewNumber:0,
      auth: authenticationService.getAuth()
    }
  },
  methods : {
    code(){
      codeService.get(this.$route.params.id).then(res => {
        this.post = res.data
      })
    },
    home(){
      router.push("/")
    },
    comment(){
      postService.comment(this.$route.params.id, {content:this.contentTextarea, codeSelection:this.selectedCode}).then(() => {
        this.contentTextarea = ''
        this.comments()
      })
    },
    comments(){
      postService.comments(this.$route.params.id, this.pageNumber).then(res => {
        this.commentsPage = res.data.comments
        this.pageNumber = res.data.pageNumber
        this.maxPageCommentNumber = res.data.maxPageNumber
        this.selectedCode = ""
      })
    },
    commentsPrev(){
      this.pageNumber -= 1
      this.comments()
    },
    commentsNext(){
      this.pageNumber += 1
      this.comments()
    },
    reviews(){
      postService.reviews(this.$route.params.id, this.pageReviewNumber, this.sortBy).then(res => {
        this.reviewsPage = res.data.reviews
        this.pageReviewNumber = res.data.pageNumber
        this.maxPageReviewNumber = res.data.maxPageNumber
      })
    },
    reviewsPrev(){
      this.pageReviewNumber -= 1
      this.reviews()
    },
    reviewsNext(){
      this.pageReviewNumber += 1
      this.reviews()
    },
    highlightCode() {
      Prism.highlightAll();
    },
    updateSelectedCode(code) {
      this.selectedCodePre = code;
    },
    addSelectedCode(){
      if (this.selectedCodePre !== ''){
        this.selectedCode = this.selectedCodePre
      }else {
        alert("Veuillez selectionner un morceau de code en le surlignant avec votre souris.")
      }
    },
    removeSelection(){
      this.selectedCode = ''
    },
    refresh(){
      this.code()
      this.comments()
      this.reviews()
    },
    newest(){
      this.sortBy = "newest";
      this.reviews();
    },
    relevance(){
      this.sortBy = "relevance";
      this.reviews();
    }
  }
}
</script>

<style scoped>
  
.post {
  background-color: #1e1e1e;
  border-radius: 10px;
  box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2);
  padding: 10px;
  margin: 20px;
  width: calc(33.3333% - 40px);
  text-align: left;
}

.postContent {
  background-color: #1e1e1e;
  border-radius: 10px;
  box-shadow: 0px 0px 10px rgba(26, 26, 26, 0.2);
  padding: 10px;
  margin: 20px;
  width: calc(100% - 80px);
  text-align: left;

}

.comments {
  background-color: #282828;
  border-radius: 10px;
  box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2); /* Ombre tout autour */
  margin: 20px;
  width: calc(33.3333% - 40px);
}

.reviews {
  background-color: #282828;
  border-radius: 10px;
  box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2); /* Ombre tout autour */
  margin: 20px;
  padding: 20px;
  width: calc(60% - 40px);
}

.row {
  display: flex;
  flex-direction: row;
}

.col {
  display: flex;
  flex-direction: column;
}

.left {
  float:left;
}

.right {
  float:right;
}


textarea{
  height: 80px;
  width: 80%;
  margin-left: 20px;
  margin-right: 15px;
  resize: none;
  border-radius: 10px;
}

.send-button{
  margin-top: 15px;
  margin-bottom: 25px;
  padding: 10px;
  border: 2px solid #ccc;
  border-radius: 10px;
}

.send-button:hover{
  background: #707070;
  color: black;
}

.prevButton{
  margin-bottom: 10px;
}

.nextButton{
  margin-bottom: 10px;
}
.select-code{
  font-size: 75%;
}

.cross-button{
  max-width: 20px;
}

.limit {
  width: 100px;
}

.add {
  margin-bottom: 10px;
}

</style>
