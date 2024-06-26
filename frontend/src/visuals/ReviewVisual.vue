<template>
  <div>
    <div class="post-header">
      <div class="post-header-info" @click="user()">
        <img v-if="photo == null" class="post-author-avatar" src="../assets/profile.jpg" alt="Author Avatar" />
        <img v-if="photo != null" class="post-author-avatar" :src="photo" alt="Author Avatar" />
        <div class="post-author-info">
          <h2>{{ post.userInformation.username }}</h2>
        </div>
      </div>
      <p class="post-date">{{ post.date }}</p>
      <div class="post-actions"></div>
    </div>
    <div @click="review()" class="element">
      <h2 class="post-title">{{ post.title }}</h2>
      <div v-for="contentReview in post.content" :key="contentReview">
        <pre v-if="contentReview.codeSelection != null && contentReview.codeSelection !== ''"><code class="language-java">{{ contentReview.codeSelection }}</code></pre>
        <pre class="post-content"><code v-html="markdownToHtml(contentReview.content)"></code></pre>
      </div>
    </div>
    <div class="post-footer">
      <div class="post-votes">
        <button v-if="auth === null || post.voteType !== 'UpVote'" class="post-button" @click="like">
          <i class="fa-regular fa-thumbs-up fa-beat" style="color: #00ffb3"></i>
        </button>
        <button v-if="auth !== null && post.voteType === 'UpVote'" class="post-button" @click="like">
          <i class="fa-solid fa-thumbs-up fa-beat" style="color: #00ffb3"></i>
        </button>
        <p style="padding-right: 7px">{{ post.score }}</p>
        <button v-if="auth === null || post.voteType !== 'DownVote'" class="post-button" @click="dislike">
          <i class="fa-regular fa-thumbs-down fa-beat" style="color: #f44e4e"></i>
        </button>
        <button v-if="auth !== null && post.voteType === 'DownVote'" class="post-button" @click="dislike">
          <i class="fa-solid fa-thumbs-down fa-beat" style="color: #f44e4e"></i>
        </button>
      </div>
      <button v-if="auth !== null && auth.role === 'ADMIN'" class="post-button" @click="del">
        <i class="fa-solid fa-trash-can fa-bounce fa-xs" style="color: #b3b2b2;"></i>
      </button>
      <div class="post-votes" @click="review()">
        <button class="post-button">
          <i class="fa-regular fa-comment" style="color: #74C0FC"></i>
        </button>
        <p style="padding-right: 7px">{{post.comments }}</p>
        <button class="post-button" >
          <i class="fa-regular fa-pen-to-square fa-2xs" style="color: #74C0FC"></i>
        </button>
        <p>{{post.reviews }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { library, dom } from "@fortawesome/fontawesome-svg-core";
import { fas } from '@fortawesome/free-solid-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons';
import { far } from '@fortawesome/free-regular-svg-icons';
import { reviewService } from "@/services/review.service";
import { postService } from "@/services/post.service";
import router from "@/router";
import MarkdownIt from "markdown-it";
import {authenticationService} from "@/services/authentication.service";
import Prism from 'prismjs';
import "prismjs/themes/prism-tomorrow.css"
import 'prismjs/components/prism-java'

library.add(fas, far, fab)
dom.watch();
export default {
  name: 'ReviewVisual',
  props: {
    post: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      auth: authenticationService.getAuth(),
      score:this.post.score,
      photo: null
    }
  },
  methods: {
    like() {
      postService.vote(this.post.id, "UpVote").then(() => {
        //this.score = res.data
        this.$emit("refresh")
      })
    },
    dislike() {
      postService.vote(this.post.id, "DownVote").then(() => {
        //this.score = res.data
        this.$emit("refresh")
      })
    },
    review() {
      router.push('/reviews/' + this.post.id)
    },
    user() {
      router.push('/profile/' + this.post.userInformation.username)
    },
    markdownToHtml(markdown) {
      const md = new MarkdownIt();
      return md.render(markdown);
    },
    highlightCode() {
      Prism.highlightAll();
    },
    del() {
      if (confirm("Êtes-vous sûr de vouloir supprimer cette revue ?")) {
        reviewService.del(this.post.id)
        this.$emit("refresh")
      }
    }
  },
  mounted(){
    this.highlightCode();
    if (this.post.userInformation.profilePhoto != null){
      this.photo = "data:image/jpg;base64," + this.post.userInformation.profilePhoto
    }
    console.log(this.post)
  }
}
</script>

<style scoped>
.post {
  background-color: #1e1e1e;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2); /* Ombre tout autour */
  padding: 20px;
  margin: 20px;
  width: calc(100% - 80px);
  text-align: left;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.post-header-info {
  display: flex;
  align-items: center;
}

.post-author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.post-author-info h2 {
  font-size: 16px;
  margin: 0;
}

.post-date {
  font-size: 14px;
  color: #666;
  margin: 0 0 0 auto;
}

.post-title {
  font-size: 20px;
  margin-bottom: 10px;
  text-align: left;
  word-wrap: break-word;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-votes {
  display: flex;
  align-items: center;
}

.post-button {
  background-color: transparent;
  border: none;
  color: white;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  font-size: 30px;
  margin-bottom: 10px;
  cursor: pointer;
  border-radius: 5px;
}

.fa-thumbs-up,
.fa-thumbs-down,
.fa-comment {
  font-size: 20px;
  margin-right: 5px;
}

.element:hover {
  cursor: pointer;
}

.post-content{
  white-space: pre-wrap;
  word-wrap: break-word;
}

</style>
