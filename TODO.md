1. Update the graph ql requests to be valid - add steps for creating blame tree.
2. Add mountebank steps to act as the ai model server for starting - to not rack up too many fees.
   - Create a postgres drive for loading a common repository indexed with machine learning algorithm.
   - Save drive and be able to load postgres from it. 
   - Add a setup step to provide the drive location of the database, and then it starts postgres from the database.
   - Add mountebank steps for responding to embedding queries by model server for adding branch, a particular blame node setup
3. Add a workflow that allows for just producing the next commit with different inputs to test it with particular commits for a repository to test the way it works.
4. Add a workflow where the model can ask for more context - start with this having a single repository, a repository with history, like an open source repository, at a particular point in time. So then you should be able to probe it in the way so as to produce tool calls by the machine learning model, with a tool to see if the machine learning model will ever produce a better result with the toolcall prompt, and whether it can produce readable mpc requests.

//

The change in the repository - i.e. if I do a commit how does the database respond? Or rebase, reset, etc. In the commit diff context. This needs to minimize embedding time. For example for rebase if the content doesn't change then should not re-embed, just change the commits. Delete orphan commit nodes on a schedule, for example if something was reset and the commits are not part of a branch saved. But then can search by the text in the change to determine whether to re-embed, and when building a new branch, for instance, or updating after rebase, search by a hash of the content to determine whether to just link or re-embed. That's the easy way, using the hash of the file to determine whether to embed, as if saying, does the text being embedded exist?


1. Be able to build the Docker containers in Gradle
2. Be able to spin up the containers so they talk to each other from the docker compose directly after building a part of it.



//

The data embedded can't be saved in the database, but a hash of it with a "location" to find it can be.