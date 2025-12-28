# Model & State Contract

## Post Model
```
Post {
  id: String (UUID, client-generated)
  type: "offer" | "request"
  authorId: String
  authorName: String
  neighborhood: String
  body: String
  likeCount: Int
  commentCount: Int
  createdAt: Timestamp
}
```

## Comment Model
```
Comment {
  id: String (UUID, client-generated)
  postId: String
  authorId: String
  authorName: String
  text: String
  createdAt: Timestamp
}
```

## FeedState
```
FeedState {
  isLoading: Boolean
  isRefreshing: Boolean
  posts: List<Post>
  error: String?
}
```

## Firestore Collections
- `posts/` - all posts
- `comments/` - all comments (filter by postId)

## Security Rules
- Read: authenticated users
- Create: authorId == auth.uid
- Update/Delete: authorId == auth.uid
