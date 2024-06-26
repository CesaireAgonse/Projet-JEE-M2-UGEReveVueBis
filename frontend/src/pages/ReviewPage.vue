<template>
  <div class="col">
    <button class="basic-button left limit" @click="home">
      <i>
        <i class="fa-solid fa-arrow-left"></i>
        Home
      </i>
    </button>
    <button v-if="post != null && post.typePost === 'Code'" class="basic-button left limit" @click="codeReviewed()">
      <i>
        <i class="fa-solid fa-arrow-left"></i>
        Code
      </i>
    </button>
    <button v-if="post != null && post.typePost === 'Review'" class="basic-button left limit" @click="reviewReviewed()">
      <i>
        <i class="fa-solid fa-arrow-left"></i>
        Review
      </i>
    </button>
    <div class="row">
      <div class="post">
        <ReviewVisual :post="post" v-if="post != null" @refresh="refreshBack"></ReviewVisual>
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
        <div class="row">
          <textarea v-model="contentTextarea" placeholder="Entrez votre texte ici"></textarea>
          <div class="send-button" @click="comment()">
            <i class="fa-regular fa-paper-plane fa-2xl" style="color: #ffffff;"></i>
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
import CommentVisual from "@/visuals/CommentVisual.vue";
import ReviewVisual from "@/visuals/ReviewVisual.vue";
import { reviewService } from "@/services/review.service";
import { postService } from "@/services/post.service";
import router from "@/router";
import { library, dom } from "@fortawesome/fontawesome-svg-core";
import { fas } from '@fortawesome/free-solid-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons';
import { far } from '@fortawesome/free-regular-svg-icons';
import ReviewForm from "@/components/ReviewForm.vue";
import {authenticationService} from "@/services/authentication.service";
library.add(fas, far, fab)
dom.watch();

export default {
  components: {ReviewForm, CommentVisual, ReviewVisual},
  watch: {
    '$route.params.id': function() {
      this.comments()
      this.reviews()
      this.code()
    }
  },
  mounted() {
    document.title = "Review"
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
      commentsPage:null,
      pageNumber:0,
      reviewsPage:null,
      pageReviewNumber:0,
      maxPageCommentNumber:0,
      maxPageReviewNumber:0,
      auth: authenticationService.getAuth(),
      sortBy:''
    }
  },
  methods : {
    code(){
      reviewService.get(this.$route.params.id).then(res => {
        this.post = res.data
        console.log(this.post)
      })
    },
    home(){
      router.push("/")
    },
    comment(){
      postService.comment(this.$route.params.id, {content:this.contentTextarea, codeSelection:null}).then(() => {
        this.contentTextarea = ''
        this.comments()
      })
    },
    review(){
      postService.review(this.$route.params.id, {content:this.reviewTextarea}).then(() => {
        this.reviewTextarea = ''
        this.reviews()
      })
    },
    comments(){
      postService.comments(this.$route.params.id, this.pageNumber).then(res => {
        this.commentsPage = res.data.comments
        this.pageNumber = res.data.pageNumber
        this.maxPageCommentNumber = res.data.maxPageNumber
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
      postService.reviews(this.$route.params.id, this.pageReviewNumber,  this.sortBy).then(res => {
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
    refresh(){
      this.reviews()
    },
    reviewReviewed(){
      router.push("/reviews/" + this.post.idPost)
    },
    codeReviewed(){
      router.push("/codes/" + this.post.idPost)
    },
    refreshBack(){
      if (this.post != null && this.post.typePost === 'Code'){
        this.codeReviewed()
      }
      if (this.post != null && this.post.typePost === 'Review') {
        this.reviewReviewed()
      }
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

.prevButton{
  margin-bottom: 10px;
}

.nextButton{
  margin-bottom: 10px;
}

.limit {
  width: 100px;
}


</style>
